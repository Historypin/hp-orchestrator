package sk.eea.td.console.controller;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.Task;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.model.*;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.console.repository.UsersRepository;
import sk.eea.td.mapper.JobToTaskFormMapper;
import sk.eea.td.mapper.TaskFormtoJobMapper;
import sk.eea.td.rest.service.EuropeanaCsvFileValidationService;
import sk.eea.td.rest.validation.CsvFileValidationException;
import sk.eea.td.util.DateUtils;
import sk.eea.td.util.ParamUtils;
import sk.eea.td.util.PathUtils;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static sk.eea.td.console.model.ParamKey.EU_CSV_FILE;

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

    @Autowired
    private TaskFormtoJobMapper taskFormtoJobMapper;

    @Autowired
    private JobToTaskFormMapper jobToTaskFormMapper;

    @Autowired
    private EuropeanaCsvFileValidationService validationService;

    @ModelAttribute("allHarvestTypes")
    public TaskForm.HarvestType[] populateHarvestTypes() {
        return TaskForm.HarvestType.values();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String indexView(@RequestParam(name = "edit", required = false) Long jobId, Model model) {
        if (jobId != null) {
            final Job job = jobRepository.findOne(jobId);
            if (job != null) {
                final Set<Param> paramList = paramRepository.findByJob(job);
                model.addAttribute("taskForm", jobToTaskFormMapper.map(job, paramList));
                return "index";
            }
        }
        model.addAttribute("taskForm", new TaskForm());
        return "index";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String indexSubmit(@Valid @ModelAttribute TaskForm taskForm,
            BindingResult bindingResult,
            Principal principal) throws IOException {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        if (Flow.FLOW_1.equals(taskForm.getFlow())) {
            // validate date
            if (!DateUtils.isHistorypinDateValid(taskForm.getCollectionDate())) {
                bindingResult.rejectValue("collectionDate", "parseError.collectionDate");
                return "index";
            }
        }

        if (taskForm.getJobId() != null) { // we are editing item
            Job job = jobRepository.findOne(taskForm.getJobId());
            if (job != null) {
                Param csvFile = null;
                if (TaskForm.HarvestType.CSV_FILE.equals(taskForm.getHarvestType())) {
                    if (taskForm.getCsvFile().isEmpty()) {
                        Optional<Param> csvFileParamOptional = job.getParams().stream().filter(e -> e.getKey().equals(ParamKey.EU_CSV_FILE)).findFirst();
                        if(csvFileParamOptional.isPresent()) {
                            csvFile = csvFileParamOptional.get();
                        } else {
                            throw new IllegalStateException("Could not find CSV file when editing job!");
                        }
                    } else {
                        if(!isCSVFileValid(taskForm, bindingResult)) {
                            return "index";
                        } else {
                            csvFile = new BlobParam(EU_CSV_FILE, taskForm.getCsvFile().getOriginalFilename(), taskForm.getCsvFile().getBytes());
                        }
                    }
                }

                // delete old params
                job.getParams().clear();
                job = jobRepository.save(job);

                job = taskFormtoJobMapper.map(job, taskForm);
                if (TaskForm.HarvestType.CSV_FILE.equals(taskForm.getHarvestType())) {
                    job.addParam(csvFile);
                }
                job = jobRepository.save(job);

                LOG.info("Edited job id= {}.", job.getId());
                return "redirect:/tasks";
            }
        } else { // we are creating new item
            if (TaskForm.HarvestType.CSV_FILE.equals(taskForm.getHarvestType())) {
                if (taskForm.getCsvFile().isEmpty()) {
                    bindingResult.rejectValue("csvFile", "csvFile.empty");
                    return "index";
                }
                if(!isCSVFileValid(taskForm, bindingResult)) {
                    return "index";
                }
            }
            Job job = taskFormtoJobMapper.map(taskForm);
            job.setUser(usersRepository.findByUsername(principal.getName()));
            job = jobRepository.save(job);

            LOG.info("Created job id= {}.", job.getId());
            JobRun jobRun = new JobRun();
            jobRun.setJob(job);
            jobRun.setStatus(JobRun.JobRunStatus.NEW);
            ParamUtils.copyParamsIntoJobRun(job.getParams(), jobRun);
            jobRunRepository.save(jobRun);
            return "redirect:/tasks";
        }

        return "index";
    }

    private boolean isCSVFileValid(TaskForm taskForm, BindingResult bindingResult) throws IOException {
        File file = new File(taskForm.getCsvFile().getOriginalFilename());
        FileUtils.touch(file);
        FileUtils.writeByteArrayToFile(file, taskForm.getCsvFile().getBytes());
        try {
            validationService.validate(file);
        } catch (CsvFileValidationException e) {
            String faultLines = e.getFaultLines().stream().sorted().map(Object::toString).collect(Collectors.joining(", "));
            if(e.isFaultLinesOverflow()) {
                bindingResult.rejectValue("csvFile", "csvFile.not.valid.more", new Object[] {faultLines}, "");
            } else {
                bindingResult.rejectValue("csvFile", "csvFile.not.valid", new Object[] {faultLines}, "");
            }
            return false;
        } catch (IOException e) {
            LOG.error("Exception at CSV file validation.", e);
            bindingResult.rejectValue("csvFile", "csvFile.read.error");
            return false;
        }
        return true;
    }
}
