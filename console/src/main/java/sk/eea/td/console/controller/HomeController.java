package sk.eea.td.console.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Param;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.console.repository.UsersRepository;
import sk.eea.td.console.model.Connector;
import sk.eea.td.util.DateUtils;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Set;

import static sk.eea.td.console.model.ParamKey.*;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class HomeController {

    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired
    private ParamRepository paramRepository;

    @Autowired
    private UsersRepository usersRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String indexView(TaskForm taskForm) {
        return "index";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String indexSubmit(@Valid @ModelAttribute TaskForm taskForm, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        Job job = new Job();
        job.setName(taskForm.getName());
        job.setUser(usersRepository.findByUsername(principal.getName()));
        job.setSource(taskForm.getFlow().getSource());
        job.setTarget(taskForm.getFlow().getTarget());

        if (Connector.HISTORYPIN.equals(job.getTarget())) {
            // validate date and tags
            if (!DateUtils.isHistorypinDateValid(taskForm.getCollectionDate())) {
                bindingResult.rejectValue("collectionDate", "parseError.collectionDate");
                return "index";
            }
            job.addParam(new Param(HP_USER_ID, taskForm.getHistorypinUserId().toString()));
            job.addParam(new Param(HP_API_KEY, taskForm.getHistorypinApiKey()));
            job.addParam(new Param(HP_API_SECRET, taskForm.getHistorypinApiSecret()));
            job.addParam(new Param(HP_DATE, taskForm.getCollectionDate()));
            job.addParam(new Param(HP_TAGS, taskForm.getCollectionTags()));
            job.addParam(new Param(HP_NAME, taskForm.getCollectionName()));
            job.addParam(new Param(HP_LAT, taskForm.getCollectionLat().toString()));
            job.addParam(new Param(HP_LNG, taskForm.getCollectionLng().toString()));
            job.addParam(new Param(HP_RADIUS, taskForm.getCollectionRadius().toString()));
        }

        if (Connector.EUROPEANA.equals(job.getSource())) {
            job.addParam(new Param(EU_REST_QUERY, taskForm.getLuceneQuery()));
            job.addParam(new Param(EU_REST_FACET, taskForm.getSearchFacet()));
        }

        if (Connector.HISTORYPIN.equals(job.getSource())) {
            job.addParam(new Param(HP_PROJECT_SLUG, taskForm.getProjectSlug()));
        }

        jobRepository.save(job);

        LOG.info("Created job id= {}.", job.getId());
        JobRun jobRun = new JobRun();
        jobRun.setJob(job);
        jobRun.setStatus(JobRun.JobRunStatus.NEW);
        Set<Param> paramList = paramRepository.findByJob(job);
        for (Param param : paramList) {
            jobRun.addReadOnlyParam(new ReadOnlyParam(param));
        }
        jobRunRepository.save(jobRun);
        return "redirect:/?success=true";
    }
}
