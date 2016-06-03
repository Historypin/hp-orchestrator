package sk.eea.td.mapper;

import org.springframework.stereotype.Component;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.Param;
import sk.eea.td.util.DateUtils;

import static sk.eea.td.console.model.ParamKey.*;
import static sk.eea.td.console.model.ParamKey.EU_REST_FACET;
import static sk.eea.td.console.model.ParamKey.HP_PROJECT_SLUG;

@Component
public class TaskFormtoJobMapper {

    public Job map(TaskForm taskForm) {
        return this.map(new Job(), taskForm);
    }

    public Job map(Job job, TaskForm form) {
        job.setName(form.getName());
        
        job.setSource(form.getFlow().getSource());
        job.setTarget(form.getFlow().getTarget());

        if (Connector.HISTORYPIN.equals(job.getTarget())) {
            job.addParam(new Param(HP_USER_ID, form.getHistorypinUserId().toString()));
            job.addParam(new Param(HP_API_KEY, form.getHistorypinApiKey()));
            job.addParam(new Param(HP_API_SECRET, form.getHistorypinApiSecret()));
            job.addParam(new Param(HP_DATE, form.getCollectionDate()));
            job.addParam(new Param(HP_TAGS, form.getCollectionTags()));
            job.addParam(new Param(HP_NAME, form.getCollectionName()));
            job.addParam(new Param(HP_LAT, form.getCollectionLat().toString()));
            job.addParam(new Param(HP_LNG, form.getCollectionLng().toString()));
            job.addParam(new Param(HP_RADIUS, form.getCollectionRadius().toString()));
        }

        if (Connector.EUROPEANA.equals(job.getSource())) {
            job.addParam(new Param(EU_REST_QUERY, form.getLuceneQuery()));
            job.addParam(new Param(EU_REST_FACET, form.getSearchFacet()));
        }

        if (Connector.HISTORYPIN.equals(job.getSource())) {
            job.addParam(new Param(HP_PROJECT_SLUG, form.getProjectSlug()));
        }

        return job;
    }
}
