package sk.eea.td.mapper;

import org.springframework.stereotype.Component;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static sk.eea.td.console.model.ParamKey.*;

@Component
public class JobToTaskFormMapper {

    public TaskForm map(Job job, Set<Param> paramList) {
        final Map<ParamKey, String> paramMap = new HashMap<>();
        paramList.stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));

        final TaskForm form = new TaskForm();
        form.setJobId(job.getId());
        form.setName(job.getName());
        form.setFlow(Flow.getFlow(job.getSource(), job.getTarget()));

        if(Connector.HISTORYPIN.equals(job.getTarget())) {
            form.setHistorypinUserId(Long.parseLong(paramMap.get(HP_USER_ID)));
            form.setHistorypinApiKey(paramMap.get(HP_API_KEY));
            form.setHistorypinApiSecret(paramMap.get(HP_API_SECRET));
            form.setCollectionDate(paramMap.get(HP_DATE));
            form.setCollectionTags(paramMap.get(HP_TAGS));
            form.setCollectionName(paramMap.get(HP_NAME));
            form.setCollectionLat(Double.parseDouble(paramMap.get(HP_LAT)));
            form.setCollectionLng(Double.parseDouble(paramMap.get(HP_LNG)));
            form.setCollectionRadius(Long.parseLong(paramMap.get(HP_RADIUS)));
        }

        if(Connector.EUROPEANA.equals(job.getSource())) {
            form.setLuceneQuery(paramMap.get(EU_REST_QUERY));
            form.setSearchFacet(paramMap.get(EU_REST_FACET));
        }

        if(Connector.HISTORYPIN.equals(job.getSource())) {
            form.setProjectSlug(paramMap.get(HP_PROJECT_SLUG));
        }

        return form;
    }
}
