package sk.eea.td.console.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.model.Destination;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.Param;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.rest.model.Connector;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class HomeController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ParamRepository paramRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String indexView(TaskForm taskForm) {
        return "index";
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
        jobRepository.save(job);

        if(taskForm.getDestinations().contains(Destination.HP)) {
            Param param = new Param("collectionName", taskForm.getCollectionName());
            job.addParam(param);
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

        return "redirect:/?success=true";
    }
}
