package sk.eea.td.console.controller;

import java.security.Principal;
import java.util.Set;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.model.Flow;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Param;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.console.repository.UsersRepository;
import sk.eea.td.mapper.JobToTaskFormMapper;
import sk.eea.td.mapper.TaskFormtoJobMapper;
import sk.eea.td.util.DateUtils;

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
            Principal principal) {
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
                // delete old params
                job.getParams().clear();
                job = jobRepository.save(job);

                job = taskFormtoJobMapper.map(job, taskForm);
                job = jobRepository.save(job);
                LOG.info("Edited job id= {}.", job.getId());
                return "redirect:/tasks";
            }
        } else { // we are creating item
            final Job job = taskFormtoJobMapper.map(taskForm);
            job.setUser(usersRepository.findByUsername(principal.getName()));
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
            return "redirect:/tasks";
        }

        return "index";
    }
}
