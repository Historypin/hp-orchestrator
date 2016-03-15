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
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.Param;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.console.repository.ProcessRepository;
import sk.eea.td.console.repository.ReadOnlyParamRepository;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class HomeController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ParamRepository paramRepository;


    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ReadOnlyParamRepository readOnlyParamRepository;

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
        job.setSource(taskForm.getHarvesting().toString());
        job.setTarget(taskForm.getDestinations().stream().map(TaskForm.Destination::toString).collect(Collectors.joining(", ")));
        jobRepository.save(job);

        if(taskForm.getDestinations().contains(TaskForm.Destination.HP)) {
            Param param = new Param("collectionName", taskForm.getCollectionName(), job);
            paramRepository.save(param);
        }

        if (TaskForm.Harvesting.EU == taskForm.getHarvesting()) {
            if(TaskForm.Type.OAIPMH == taskForm.getType()) {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

                Param param = new Param("from", format.format(taskForm.getOaiFrom()), job);
                paramRepository.save(param);
                param = new Param("until", format.format(taskForm.getOaiUntil()), job);
                paramRepository.save(param);
                param = new Param("set", taskForm.getOaiSet(), job);
                paramRepository.save(param);
                param = new Param("metadataPrefix", taskForm.getOaiMetadataPrefix(), job);
                paramRepository.save(param);paramRepository.save(param);
            } else { // Europeana REST
                Param param = new Param("luceneQuery", taskForm.getLuceneQuery(), job);
                paramRepository.save(param);
            }
        } else {
            // Harvesting from Historypin
            Param param = new Param("projectSlug", taskForm.getProjectSlug(), job);
            paramRepository.save(param);
        }

        return "redirect:/?success=true";
    }
}
