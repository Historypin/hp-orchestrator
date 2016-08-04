package sk.eea.td.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.eu_client.api.EuropeanaClient;
import sk.eea.td.hp_client.api.Location;
import sk.eea.td.hp_client.api.Pin;
import sk.eea.td.hp_client.api.PinnerType;
import sk.eea.td.console.model.dto.HistorypinTransformDTO;
import sk.eea.td.rest.service.PlacesCache;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static sk.eea.td.util.DateUtils.parseHistoryPinDate;

@Component
public class EuropeanaToHistorypinMapper {

    private static final Logger LOG = LoggerFactory.getLogger(EuropeanaToHistorypinMapper.class);

    @Value("${historypin.europeana.provider.id}")
    private String europeanaProviderId;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EuropeanaClient europeanaClient;

    @Autowired
    private PlacesCache placesCache;

    /**
     * Method maps object from europeana to historypin.
     *
     * It does it in fail-safe manner. In case of problem, it skips record from the file to transform.
     *
     * @param harvestedFile File that contains records to transform.
     * @param transformToFile File to save in the resulting transformation.
     * @param paramMap  parameters map.
     * @return false if one or more pins could not be transformed.
     * @throws IOException
     */
    public boolean map(Path harvestedFile, Path transformToFile, Map<ParamKey, String> paramMap) throws IOException {
        boolean allItemTransformed = true;

        JsonNode rootNode = objectMapper.readTree(harvestedFile.toFile());
        List<Pin> pins = new ArrayList<>();
        for (JsonNode record : rootNode.get("items")) {
            final Pin pin = new Pin();
            try {
                final String remoteId = getRequiredFieldValue("id", record, null);
                pin.setRemoteId(remoteId);

                final String link = getRequiredFieldValue("guid", record, remoteId);
                pin.setLink(link);

                final String caption = getRequiredFieldValueFromArray("title", record, remoteId);
                pin.setCaption(caption);

                final String description = getRequiredFieldValueFromArray("dcDescription", record, remoteId);
                pin.setDescription(description);

                // content validation nad mapping
                String content = getFieldValueFromArray("edmIsShownBy", record);
                if (isEmpty(content)) {
                    // try to search for content in items detail
                    try {
                        content = europeanaClient.getRecordsEdmIsShownBy(remoteId);
                    } catch (Exception e) {
                        LOG.error("Exception at retrieving record details from Europeana API", e);
                        allItemTransformed = false;
                        continue;
                    }

                    if (isNotEmpty(content)) {
                        pin.setContent(content);
                    } else {
                        LOG.error("Content of pin is empty. Record with remoteId: '{}' will be skipped.",
                                remoteId);
                        allItemTransformed = false;
                        continue;
                    }
                } else {
                    pin.setContent(content);
                }

                // set europeana ID as remote provider
                pin.setRemoteProviderId(europeanaProviderId);

                // type mapping
                final String type = getRequiredFieldValue("type", record, remoteId);
                switch (type) { // fall-through logic intended
                    case "TEXT":
                    case "IMAGE":
                        pin.setPinnerType(PinnerType.PHOTO);
                        break;
                    case "SOUND":
                    case "VIDEO":
                        pin.setPinnerType(PinnerType.MEDIA);
                    default:
                        LOG.error("Type: '{}' is not recognized. Record with remoteId: '{}' will be skipped.",
                                type, remoteId);
                        allItemTransformed = false;
                        continue;
                }

                // date mapping
                String date = parseHistoryPinDate(getFieldValueFromArray("year", record));
                if (isEmpty(date)) {
                    date = paramMap.get(ParamKey.HP_DATE);
                }
                pin.setDate(date);

                // license mapping
                String license = getRequiredFieldValueFromArray("rights", record, remoteId);
                switch (license) { // fall-through logic intended
                    case "http://www.europeana.eu/rights/rr-p/":
                        license = "copyright";
                        break;
                    case "http://www.europeana.eu/rights/out-of-copyright-non-commercial/":
                        license = "no-copyright";
                        break;
                    case "http://creativecommons.org/licenses/by-nd/3.0/":
                        license = "http://creativecommons.org/licenses/by-nd/2.0/";
                        break;
                    case "http://www.europeana.eu/rights/rr-f/":
                        license = "open-government";
                        break;
                    case "http://creativecommons.org/publicdomain/mark/1.0/":
                        license = "public-domain";
                        break;
                    case "http://creativecommons.org/publicdomain/zero/1.0/":
                    case "http://creativecommons.org/licenses/by/3.0/":
                    case "http://creativecommons.org/licenses/by-nc/3.0/":
                    case "http://creativecommons.org/licenses/by-sa/3.0/":
                    case "http://creativecommons.org/licenses/by-nc-nd/3.0/":
                    case "http://creativecommons.org/licenses/by-nc-sa/3.0/":
                    default:
                        // leave license as it is
                        break;
                }
                pin.setLicense(license);

                // tags mapping
                pin.setTags(paramMap.get(ParamKey.HP_TAGS));

                // location mapping
                final String edmPlaceLatitude = getFieldValueFromArray("edmPlaceLatitude", record);
                final String edmPlaceLongitude = getFieldValueFromArray("edmPlaceLongitude", record);
                if (isNotEmpty(edmPlaceLatitude) && isNotEmpty(edmPlaceLongitude)) { // if exact location is known, use it
                    try {
                        Location location = new Location();
                        location.setLat(Double.parseDouble(edmPlaceLatitude));
                        location.setLng(Double.parseDouble(edmPlaceLongitude));
                        location.setRange(0L); // range=0 means that exact location is known
                        pin.setLocation(location);
                    } catch (NumberFormatException e) {
                        // pass on
                    }
                }

                final String country = getFieldValueFromArray("country", record);
                if (pin.getLocation() == null) {
                    if (isNotEmpty(country)) { // try to get location from country
                        pin.setLocation(placesCache.getLocation(country));
                    }
                }


                if (pin.getLocation() == null || pin.getLocation().getLat() == null) { // if location is still unknown, use collections default
                    pin.setLocation(new Location(
                            Double.parseDouble(paramMap.get(ParamKey.HP_LAT)),
                            Double.parseDouble(paramMap.get(ParamKey.HP_LNG)),
                            Long.parseLong(paramMap.get(ParamKey.HP_RADIUS))
                    ));
                }

            } catch (MissingRequiredFieldException e) {
                LOG.error("Exception at mapping. Record will be skipped.", e);
                allItemTransformed = false;
                continue;
            }

            pins.add(pin);
        }

        final HistorypinTransformDTO historypinTransformDTO = new HistorypinTransformDTO();
        historypinTransformDTO.setPins(pins);
        objectMapper.writeValue(new FileOutputStream(transformToFile.toFile()), historypinTransformDTO);
        LOG.debug("File '{}' has been transformed into file: '{}'", harvestedFile.toString(), transformToFile.toString());

        return allItemTransformed;
    }

    /**
     * Returns text value from Json field with provided name.
     *
     * @param fieldName name of field in JSON
     * @param jsonNode node look for in
     * @return text value from Json field or null if no such field is found
     */
    @SuppressWarnings("unused")
    private String getFieldValue(String fieldName, JsonNode jsonNode) {
        final JsonNode node = jsonNode.get(fieldName);
        if(node == null) {
            return null;
        } else {
            return node.asText();
        }
    }

    /**
     * Return first value as text from Json array with provided name.
     *
     * @param fieldName name of field in JSON
     * @param jsonNode node look for in
     * @return first text value from Json array or null if no such field is found
     */
    private String getFieldValueFromArray(String fieldName, JsonNode jsonNode) {
        final JsonNode node = jsonNode.get(fieldName);
        if(node != null) {
            Iterator<JsonNode> iterator = node.elements();
            if(iterator.hasNext()) {
                return iterator.next().asText();
            }
        }
        return null;
    }

    /**
     * Returns text value from Json field with provided name.
     *
     * If remoteId exists for this field, it should be provided for purposes of audit or debugging.
     *
     * @param fieldName name of field in JSON
     * @param jsonNode node look for in
     * @param remoteId ID of object for audit purposes
     * @return text value from Json field
     * @throws MissingRequiredFieldException if not such field is found
     */
    private String getRequiredFieldValue(String fieldName, JsonNode jsonNode, String remoteId) throws MissingRequiredFieldException {
        final JsonNode node = jsonNode.get(fieldName);
        if(node != null) {
            return node.asText();
        } else {
            throw new MissingRequiredFieldException(fieldName, remoteId);
        }
    }

    /**
     * Return first value as text from Json array with provided name.
     *
     * @param fieldName name of field in JSON
     * @param jsonNode node look for in
     * @param remoteId ID of object for audit purposes
     * @return first Text value from Json array
     * @throws MissingRequiredFieldException if not such field is found
     */
    private String getRequiredFieldValueFromArray(String fieldName, JsonNode jsonNode, String remoteId) throws MissingRequiredFieldException {
        final JsonNode node = jsonNode.get(fieldName);
        if(node != null) {
            Iterator<JsonNode> iterator = node.elements();
            if(iterator.hasNext()) {
                return iterator.next().asText();
            }
        }
        throw new MissingRequiredFieldException(fieldName, remoteId);
    }
}
