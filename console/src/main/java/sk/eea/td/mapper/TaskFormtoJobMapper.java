package sk.eea.td.mapper;

import org.springframework.stereotype.Component;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.model.BlobParam;
import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.StringParam;

import java.io.IOException;

import static sk.eea.td.console.model.ParamKey.*;

@Component
public class TaskFormtoJobMapper {

    public Job map(TaskForm taskForm) throws IOException {
        return this.map(new Job(), taskForm);
    }

    public Job map(Job job, TaskForm form) throws IOException {
        job.setName(form.getName());

        job.setSource(form.getFlow().getSource());
        job.setTarget(form.getFlow().getTarget());

        if (Connector.HISTORYPIN.equals(job.getTarget())) {
            job.addParam(new StringParam(HP_USER_ID, form.getHistorypinUserId().toString()));
            job.addParam(new StringParam(HP_API_KEY, form.getHistorypinApiKey()));
            job.addParam(new StringParam(HP_API_SECRET, form.getHistorypinApiSecret()));
            job.addParam(new StringParam(HP_DATE, form.getCollectionDate()));
            job.addParam(new StringParam(HP_TAGS, form.getCollectionTags()));
            job.addParam(new StringParam(HP_NAME, form.getCollectionName()));
            job.addParam(new StringParam(HP_LAT, form.getCollectionLat().toString()));
            job.addParam(new StringParam(HP_LNG, form.getCollectionLng().toString()));
            job.addParam(new StringParam(HP_RADIUS, form.getCollectionRadius().toString()));
        }

        if (Connector.HISTORYPIN.equals(job.getSource())) {
            job.addParam(new StringParam(HP_PROJECT_SLUG, form.getProjectSlug()));
        }

        if (TaskForm.HarvestType.CSV_FILE.equals(form.getHarvestType())) {
            if (form.getCsvFileName() == null) {
                job.addParam(new BlobParam(EU_CSV_FILE, form.getCsvFile().getOriginalFilename(), form.getCsvFile().getBytes()));
            }
        } else {
            job.addParam(new StringParam(EU_REST_QUERY, form.getLuceneQuery()));
            job.addParam(new StringParam(EU_REST_FACET, form.getSearchFacet()));
        }

        return job;
    }
}
