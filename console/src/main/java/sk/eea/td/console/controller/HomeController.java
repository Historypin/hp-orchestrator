package sk.eea.td.console.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.bind.annotation.ResponseBody;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.form.TaskRow;
import sk.eea.td.console.model.Destination;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Param;
import sk.eea.td.console.model.datatables.DataTablesInput;
import sk.eea.td.console.model.datatables.DataTablesOutput;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.rest.model.Connector;

import static sk.eea.td.util.PageUtils.getPageable;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class HomeController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired
    private ParamRepository paramRepository;

    @Value(value = "${google.maps.api.key}")
    private String googleMapsApiKey;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String indexView(TaskForm taskForm, Model model) {
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);
        return "index";
    }

    @ResponseBody
    @RequestMapping(value = "/get.jobs", method = RequestMethod.GET)
    public DataTablesOutput<TaskRow> getJobs(@Valid DataTablesInput input) {
        DataTablesOutput<TaskRow> output = new DataTablesOutput<>();
        output.setDraw(input.getDraw());

        List<TaskRow> tasks = new ArrayList<>();
        Page<Job> jobPage = jobRepository.findAll(getPageable(input));
        for(Job job : jobPage) {
            if(job.getLastJobRun() == null) {
                tasks.add(new TaskRow(job.getName(), job.getSource().toString(), job.getTarget(), "PLANNED", "", ""));
            } else {
                tasks.add(
                        new TaskRow(job.getName(),
                                job.getSource().toString(),
                                job.getTarget(),
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


    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String indexSubmit(@ModelAttribute @Valid TaskForm taskForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        Job job = new Job();
        job.setName(taskForm.getName());
        if(TaskForm.Harvesting.EU.equals(taskForm.getHarvesting())) {
            if(TaskForm.Type.REST.equals(taskForm.getType())) {
                job.setSource(Connector.EUROPEANA);
            } else { // OAI-PMH
                job.setSource(Connector.OAIPMH);
            }
        } else { //HP
            job.setSource(Connector.HISTORYPIN);
        }
        job.setTarget(taskForm.getDestinations().stream().map(Destination::toString).collect(Collectors.joining(", ")));

        if(taskForm.getDestinations().contains(Destination.HP)) {
            job.addParam(new Param("collectionName", taskForm.getCollectionName()));
            job.addParam(new Param("collectionLat", taskForm.getCollectionLat().toString()));
            job.addParam(new Param("collectionLng", taskForm.getCollectionLng().toString()));
            job.addParam(new Param("collectionRadius", taskForm.getCollectionRadius().toString()));
        }

        if (TaskForm.Harvesting.EU == taskForm.getHarvesting()) {
            if(TaskForm.Type.OAIPMH == taskForm.getType()) {
                //DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // TODO: fix time zone, previous was crushing in OAI-PMH harvesting

                job.addParam(new Param("from", format.format(taskForm.getOaiFrom())));
                job.addParam(new Param("until", format.format(taskForm.getOaiUntil())));
                job.addParam(new Param("set", taskForm.getOaiSet()));
                job.addParam(new Param("metadataPrefix", taskForm.getOaiMetadataPrefix()));
            } else { // Europeana REST
                job.addParam(new Param("luceneQuery", taskForm.getLuceneQuery()));
            }
        } else {
            // Harvesting from Historypin
            job.addParam(new Param("projectSlug", taskForm.getProjectSlug()));
        }

        jobRepository.save(job);
        return "redirect:/?success=true";
    }
}
