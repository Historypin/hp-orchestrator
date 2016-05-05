package sk.eea.td.console.controller;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.form.TaskRow;
import sk.eea.td.console.model.Destination;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Param;
import sk.eea.td.console.model.datatables.DataTablesInput;
import sk.eea.td.console.model.datatables.DataTablesOutput;
import sk.eea.td.console.model.datatables.RestartTaskRequest;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.console.repository.UsersRepository;
import sk.eea.td.rest.model.Connector;
import sk.eea.td.util.DateUtils;

import javax.validation.Valid;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static sk.eea.td.console.model.ParamKey.*;
import static sk.eea.td.util.PageUtils.getPageable;

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

    @ResponseBody
    @RequestMapping(value = "/get.jobs", method = RequestMethod.GET)
    public DataTablesOutput<TaskRow> getJobs(@Valid DataTablesInput input) {
        DataTablesOutput<TaskRow> output = new DataTablesOutput<>();
        output.setDraw(input.getDraw());

        List<TaskRow> tasks = new ArrayList<>();
        Page<Job> jobPage = jobRepository.findAll(getPageable(input));
        for (Job job : jobPage) {
            if (job.getLastJobRun() == null) {
                tasks.add(new TaskRow(job.getName(), job.getSource().toString(), job.getTarget().toString(), "PLANNED", "", ""));
            } else {
                tasks.add(
                        new TaskRow(job.getName(),
                                job.getSource().toString(),
                                job.getTarget().toString(),
                                job.getLastJobRun().getStatus().toString(),
                                (job.getLastJobRun().getResult() != null) ? job.getLastJobRun().getResult().toString() : "",
                                job.getLastJobRun().getId().toString()
                        )
                );
            }
        }
        output.setData(tasks);
        output.setRecordsTotal(jobPage.getTotalElements());
        output.setRecordsFiltered((long) jobPage.getNumberOfElements());

        return output;
    }

    @ResponseBody
    @RequestMapping(value = "/restart.task", method = RequestMethod.POST)
    public String restartTask(@RequestBody RestartTaskRequest request) {
        JobRun jobRun = jobRunRepository.findOne(request.getLastRunId());
        if(jobRun != null) {
            Job job = jobRun.getJob();
            LOG.info("Restarting job id= {}.", job.getId());
            job.setLastJobRun(null);
            jobRepository.save(job);
        }
        return "{}";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String indexSubmit(@ModelAttribute @Valid TaskForm taskForm, Principal principal, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        Job job = new Job();
        job.setName(taskForm.getName());
        job.setUser(usersRepository.findByUsername(principal.getName()));
        if (TaskForm.Harvesting.EU.equals(taskForm.getHarvesting())) {
            if (TaskForm.Type.REST.equals(taskForm.getType())) {
                job.setSource(Connector.EUROPEANA);
            } else if(TaskForm.Type.OAIPMH.equals(taskForm.getType())) { // OAI-PMH
                job.setSource(Connector.OAIPMH);
            } else {
            	throw new IllegalArgumentException("Source protocol not recognized.");
            }
            
        } else if(TaskForm.Harvesting.HP.equals(taskForm.getHarvesting())){ //HP
            job.setSource(Connector.HISTORYPIN);
        } else if(TaskForm.Harvesting.HP_ANNOTATION.equals(taskForm.getHarvesting())){ // HP ANOTATION
        	job.setSource(Connector.EUROPEANA_ANNOTATION);
        } else {
        	throw new IllegalArgumentException("Source type not recognized");
        }
        //FIXME
        job.setTarget(taskForm.getDestinations().stream().map(Destination::toString).collect(Collectors.joining(", ")));

        if (taskForm.getDestinations().contains(Destination.HP)) {
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

        if (TaskForm.Harvesting.EU == taskForm.getHarvesting()) {
            if (TaskForm.Type.OAIPMH == taskForm.getType()) {
                //DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // TODO: fix time zone, previous was crushing in OAI-PMH harvesting
                job.addParam(new Param(OAI_FROM, format.format(taskForm.getOaiFrom())));
                job.addParam(new Param(OAI_UNTIL, format.format(taskForm.getOaiUntil())));
                job.addParam(new Param(OAI_SET, taskForm.getOaiSet()));
            } else if (TaskForm.Type.REST == taskForm.getType()) {
                job.addParam(new Param(EU_REST_QUERY, taskForm.getLuceneQuery()));
                job.addParam(new Param(EU_REST_FACET, taskForm.getSearchFacet()));
            } else {
                throw new NotImplementedException("Harvesting: " + taskForm.getHarvesting() + " of type: " + taskForm.getType() + " is not implemented!");
            }
        } else {
            // Harvesting from Historypin
            job.addParam(new Param(HP_PROJECT_SLUG, taskForm.getProjectSlug()));
        }

        jobRepository.save(job);
        return "redirect:/?success=true";
    }
}
