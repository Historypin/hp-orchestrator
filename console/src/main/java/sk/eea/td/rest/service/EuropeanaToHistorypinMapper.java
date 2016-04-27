package sk.eea.td.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.eu_client.api.EuropeanaClient;
import sk.eea.td.hp_client.api.Location;
import sk.eea.td.hp_client.api.PinnerType;
import sk.eea.td.rest.model.HistorypinTransformDTO;

import java.io.IOException;
import java.util.Iterator;
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
    private EuropeanaClient europeanaClient;

    @Autowired
    private PlacesCache placesCache;

    /**
     * Method maps object from europeana to historypin.
     * It does it in fail-safe manner. In case of problem, it skips and removes object from list of objects to transform.
     *
     * @param mappedObject Object to map, object is modified in mapping process.
     * @param paramMap     parameters map.
     * @return false if one or more pins could not be transformed.
     */
    public boolean map(HistorypinTransformDTO mappedObject, Map<ParamKey, String> paramMap) {
        boolean allItemTransformed = true;
        for (Iterator<HistorypinTransformDTO.Record> iterator = mappedObject.getRecords().iterator(); iterator
                .hasNext(); ) {
            final HistorypinTransformDTO.Record record = iterator.next();

            // europeana remote provider ID
            record.getPin().setRemoteProviderId(europeanaProviderId);

            // type mapping
            switch (record.getEuropeanaFields().getType()) { // fall-through logic intended
                case "TEXT":
                case "IMAGE":
                    record.getPin().setPinnerType(PinnerType.PHOTO);
                    break;
                case "SOUND":
                case "VIDEO":
                    record.getPin().setPinnerType(PinnerType.MEDIA);
                default:
                    LOG.error("Type: '{}' is not recognized. Record with remoteId: '{}' will be skipped.",
                            record.getEuropeanaFields().getType(), record.getPin().getRemoteId());
                    iterator.remove();
                    allItemTransformed = false;
                    continue;
            }

            // content validation
            if (isEmpty(record.getPin().getContent())) {
                // try to search for content in items detail
                String content = null;
                try {
                    content = europeanaClient.getRecordsEdmIsShownBy(record.getPin().getRemoteId());
                } catch (IOException | InterruptedException e) {
                    LOG.error("Exception at retrieving item details from Europeana API", e);
                }

                if (isNotEmpty(content)) {
                    record.getPin().setContent(content);
                } else {
                    LOG.error("Content of pin is empty. Record with remoteId: '{}' will be skipped.",
                            record.getPin().getRemoteId());
                    iterator.remove();
                    allItemTransformed = false;
                    continue;
                }
            }

            // date mapping
            String date = parseHistoryPinDate(record.getEuropeanaFields().getYear());
            if (isEmpty(date)) {
                date = paramMap.get(ParamKey.HP_DATE);
            }
            record.getPin().setDate(date);

            // license mapping
            String license;
            switch (record.getEuropeanaFields().getRights()) { // fall-through logic intended
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
                    license = record.getEuropeanaFields().getRights();

            }
            record.getPin().setLicense(license);

            // tags mapping
            record.getPin().setTags(paramMap.get(ParamKey.HP_TAGS));

            // location mapping
            if (isNotEmpty(record.getEuropeanaFields().getEdmPlaceLatitude()) && isNotEmpty(
                    record.getEuropeanaFields().getEdmPlaceLongitude())) { // if exact location is known, use it
                try {
                    Location location = new Location();
                    location.setLat(Double.parseDouble(record.getEuropeanaFields().getEdmPlaceLatitude()));
                    location.setLng(Double.parseDouble(record.getEuropeanaFields().getEdmPlaceLongitude()));
                    location.setRange(0L); // range=0 means that exact location is known
                    record.getPin().setLocation(location);
                } catch (NumberFormatException e) {
                    // pass on
                }
            }

            if (record.getPin().getLocation() == null && isNotEmpty(
                    record.getEuropeanaFields().getCountry())) { // try to get location from country
                record.getPin().setLocation(placesCache.getLocation(record.getEuropeanaFields().getCountry()));
            }

            if (record.getPin().getLocation() == null || record.getPin().getLocation().getLat() == null) { // if location is still unknown, use collections default
                record.getPin().setLocation(new Location(
                        Double.parseDouble(paramMap.get(ParamKey.HP_LAT)),
                        Double.parseDouble(paramMap.get(ParamKey.HP_LNG)),
                        Long.parseLong(paramMap.get(ParamKey.HP_RADIUS))
                ));
            }

        }
        return allItemTransformed;
    }
}
