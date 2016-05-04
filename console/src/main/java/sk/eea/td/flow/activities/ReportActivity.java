package sk.eea.td.flow.activities;

import org.springframework.beans.factory.annotation.Autowired;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.service.MailService;

import java.util.HashMap;
import java.util.Map;

public class ReportActivity implements Activity {

    @Autowired
    private MailService mailService;

    @Autowired
    private LogRepository logRepository;

    @Override
    public void execute(JobRun context) throws FlowException {
        final Map<String, String> emailParams = new HashMap<>();
        // prepare required params for sending emails
        emailParams.put("userName", context.getJob().getUser().getUsername());
        emailParams.put("taskName", context.getJob().getName());
        emailParams.put("taskRunId", context.getId().toString());

        for (Log log : logRepository.findByJobRunId(context.getId())) {
            if (Log.LogLevel.ERROR.equals(log.getLevel())) {
                emailParams.put("errors", "true");
                break;
            }
        }

        mailService.sendReportMail(
                context.getJob().getUser().getEmail(),
                "Orchestrator task is finished",
                emailParams
        );
    }

    @Override
    public String getName() {
        return "Report activity";
    }
}
