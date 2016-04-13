package sk.eea.td.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.rest.model.HistorypinTransformDTO;

import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static sk.eea.td.util.DateUtils.parseHistoryPinDate;

@Component
public class EuropeanaToHistorypinMapper {

    private static final Logger LOG = LoggerFactory.getLogger(EuropeanaToHistorypinMapper.class);

    /**
     * Method maps object from europeana to historypin.
     *
     * It does it in fail-safe manner. In case of problem, it skips and removes object from list of objects to transform.
     *
     * @param mappedObject Object to map, object is modified in mapping process.
     * @param paramMap parameters map.
     * @return false if one or more pins could not be transformed.
     */
    public boolean map(HistorypinTransformDTO mappedObject, Map<String, String> paramMap) {
        for( Iterator<HistorypinTransformDTO.Record> iterator = mappedObject.getRecords().iterator(); iterator.hasNext(); ) {
            final HistorypinTransformDTO.Record record = iterator.next();

            // type mapping
            switch(record.getEuropeanaFields().getType()) { // break-through logic intended
                case "TEXT":
                case "IMAGE":
                    record.getPin().setPinnerType("PHOTO");
                    break;
                case "SOUND":
                case "VIDEO":
                    record.getPin().setPinnerType("MEDIA");
                default:
                    LOG.error("Type: ''{}'' is not recognized. Record with remoteId: ''{}'' will be skipped.", record.getEuropeanaFields().getType(), record.getPin().getRemoteId());
                    iterator.remove();
                    continue;
            }

            // date mapping
            String date = parseHistoryPinDate(record.getEuropeanaFields().getYear());
            if(isEmpty(date)) {
                date = paramMap.get(ParamKey.HP_DATE.toString());
            }
            record.getPin().setDate(date);
        }
    }
}
