package sk.eea.td.console.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.form.TaskRow;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Param;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.console.model.datatables.DataTablesInput;
import sk.eea.td.console.model.datatables.DataTablesOutput;
import sk.eea.td.console.model.datatables.RestartTaskRequest;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.ParamRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static sk.eea.td.util.PageUtils.getPageable;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class TaskListController {

    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired
    private ParamRepository paramRepository;

    @RequestMapping(value = "/tasks", method = RequestMethod.GET)
    public String indexView(TaskForm taskForm) {
        return "task_list";
    }

    @ResponseBody
    @RequestMapping(value = "/tasks/get.tasks", method = RequestMethod.GET)
    public DataTablesOutput<TaskRow> getTasks(@Valid DataTablesInput input) {
        DataTablesOutput<TaskRow> output = new DataTablesOutput<>();
        output.setDraw(input.getDraw());

        List<TaskRow> tasks = new ArrayList<>();
        Page<Job> jobPage = jobRepository.findAll(getPageable(input));
        for (Job job : jobPage) {
            if (job.getLastJobRun() == null || job.getLastJobRun().getStatus() == null) {
                tasks.add(new TaskRow(job.getName(), job.getSource().toString(), job.getTarget().toString(), "PLANNED",
                        "", ""));
            } else {
                tasks.add(
                        new TaskRow(job.getName(),
                                job.getSource().toString(),
                                job.getTarget().toString(),
                                (job.getLastJobRun().getStatus() != null) ? job.getLastJobRun().getStatus().toString() :
                                        "",
                                (job.getLastJobRun().getResult() != null) ? job.getLastJobRun().getResult().toString() :
                                        "",
                                (job.getLastJobRun().getId() != null) ? job.getLastJobRun().getId().toString() : ""
                        )
                );
            }
        }
        output.setData(tasks);
        output.setRecordsTotal(jobRepository.count());
        output.setRecordsFiltered(jobPage.getTotalElements());

        return output;
    }

    @ResponseBody
    @RequestMapping(value = "/tasks/restart.task", method = RequestMethod.POST)
    public String restartTask(@RequestBody RestartTaskRequest request) {
        JobRun jobRun = jobRunRepository.findOne(request.getLastRunId());
        if (jobRun != null) {
            Job job = jobRun.getJob();
            LOG.info("Restarting job id= {}.", job.getId());
            jobRun = new JobRun();
            jobRun.setJob(job);
            jobRun.setStatus(JobRun.JobRunStatus.NEW);
            Set<Param> paramList = paramRepository.findByJob(job);
            for (Param param : paramList) {
                jobRun.addReadOnlyParam(new ReadOnlyParam(param));
            }
            jobRunRepository.save(jobRun);

            job.setLastJobRun(null);
            jobRepository.save(job);
        }
        return "{}";
    }
}
