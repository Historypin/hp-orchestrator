package sk.eea.td.mapper;

import org.springframework.stereotype.Component;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.model.*;
import sk.eea.td.util.ParamUtils;

import java.util.Map;
import java.util.Set;

import static sk.eea.td.console.model.ParamKey.*;

@Component
public class JobToTaskFormMapper {

    public TaskForm map(Job job, Set<Param> paramList) {
        final Map<ParamKey, String> stringParamMap = ParamUtils.copyStringParamsIntoStringParamMap(paramList);

        final TaskForm form = new TaskForm();
        form.setJobId(job.getId());
        form.setName(job.getName());
        form.setFlow(Flow.getFlow(job.getSource(), job.getTarget()));

        if (Connector.HISTORYPIN.equals(job.getTarget())) {
            form.setHistorypinUserId(Long.parseLong(stringParamMap.get(HP_USER_ID)));
            form.setHistorypinApiKey(stringParamMap.get(HP_API_KEY));
            form.setHistorypinApiSecret(stringParamMap.get(HP_API_SECRET));
            form.setCollectionDate(stringParamMap.get(HP_DATE));
            form.setCollectionTags(stringParamMap.get(HP_TAGS));
            form.setCollectionName(stringParamMap.get(HP_NAME));
            form.setCollectionLat(Double.parseDouble(stringParamMap.get(HP_LAT)));
            form.setCollectionLng(Double.parseDouble(stringParamMap.get(HP_LNG)));
            form.setCollectionRadius(Long.parseLong(stringParamMap.get(HP_RADIUS)));
        }

        if(stringParamMap.containsKey(EU_REST_QUERY)) {
            form.setHarvestType(TaskForm.HarvestType.LUCENE_QUERY);
            form.setLuceneQuery(stringParamMap.get(EU_REST_QUERY));
            form.setSearchFacet(stringParamMap.get(EU_REST_FACET));
        }

        if (Connector.HISTORYPIN.equals(job.getSource())) {
            form.setProjectSlug(stringParamMap.get(HP_PROJECT_SLUG));
        }

        final Map<ParamKey, BlobParam> blobParamMap = ParamUtils.copyBlobParamsIntobBlobParamMap(paramList);

        if (!blobParamMap.isEmpty()) {
            if (blobParamMap.containsKey(EU_CSV_FILE)) {
                form.setHarvestType(TaskForm.HarvestType.CSV_FILE);
                form.setCsvFileName(blobParamMap.get(EU_CSV_FILE).getBlobName());
            }
        }

        return form;
    }
}
