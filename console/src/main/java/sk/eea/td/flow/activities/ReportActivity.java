package sk.eea.td.flow.activities;

import java.util.HashMap;
import java.util.Map;

import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.BlobReadOnlyParam;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.service.MailService;
import sk.eea.td.util.ParamUtils;

public class ReportActivity implements Activity {

    @Autowired
    private MailService mailService;

    @Autowired
    private LogRepository logRepository;

    @Override
    public ActivityAction execute(AbstractJobRun context) throws FlowException {
        final Map<String, String> emailParams = new HashMap<>();
        // prepare required params for sending emails
        emailParams.put("userName", context.getJob().getUser().getUsername());
        emailParams.put("taskName", context.getJob().getName());
        emailParams.put("taskRunId", context.getId().toString());
 
        ByteArrayDataSource attachment = null;
        BlobReadOnlyParam param = ParamUtils.copyBlobReadOnlyParamsBlobParamMap(context.getReadOnlyParams()).get(ParamKey.EMAIL_ATTACHMENT);
        if(param != null){
            attachment= new ByteArrayDataSource(param.getBlobData(), "text/plain; charset=utf-8");
            attachment.setName(param.getBlobName());
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
