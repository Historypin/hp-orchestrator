package sk.eea.td.flow.activities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.service.MailService;

public class ReportActivity implements Activity {

    @Autowired
    private MailService mailService;

    @Autowired
    private LogRepository logRepository;

    @Override
    public ActivityAction execute(JobRun context) throws FlowException {
        final Map<String, String> emailParams = new HashMap<>();
        // prepare required params for sending emails
        emailParams.put("userName", context.getJob().getUser().getUsername());
        emailParams.put("taskName", context.getJob().getName());
        emailParams.put("taskRunId", context.getId().toString());
 
        File attachment;
        try {
            String attachmentPath = context.getReadOnlyParams().stream().filter(param -> param.getKey().equals(ParamKey.EMAIL_ATTACHMENT)).findFirst().get().getValue();
            attachment = new File(attachmentPath);
        } catch (NoSuchElementException e) {
            attachment = null;
        }
        
        for (Log log : logRepository.findByJobRunId(context.getId())) {
            if (Log.LogLevel.ERROR.equals(log.getLevel())) {
                emailParams.put("errors", "true");
                break;
            }
        }

        mailService.sendReportMail(
                context.getJob().getUser().getEmail(),
                "Orchestrator task is finished",
                emailParams,
                attachment
        );
        return ActivityAction.CONTINUE;
    }

    @Override
    public String getName() {
        return "Report activity";
    }
}
