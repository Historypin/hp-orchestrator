package sk.eea.td.flow.activities;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.service.MailService;

public class ApprovalSendMailActivity implements Activity {

    @Autowired
    private KeyBasedPersistenceTokenService keyBasedPersistenceTokenService;

    @Autowired
    private MailService mailService;

    @Value("${hostname}")
    private String hostname;

    @Value("${review.link.template}")
    private String reviewLinkTemplate;

    @Override
    public void execute(JobRun context) throws FlowException {

        String token = keyBasedPersistenceTokenService.allocateToken(context.getId().toString()).getKey();
        String link = MessageFormat.format(reviewLinkTemplate, hostname, token);

        final Map<String, String> emailParams = new HashMap<>();
        // prepare required params for sending emails
        emailParams.put("userName", context.getJob().getUser().getUsername());
        emailParams.put("taskName", context.getJob().getName());
        emailParams.put("taskRunId", context.getId().toString());
        emailParams.put("reviewLink", link);

        mailService.sendReviewMail(
                context.getJob().getUser().getEmail(),
                "Review of the HistoryPin collection enrichment",
                emailParams
        );
    }

    @Override
    public String getName() {
        return "ApprovalSendMailActivity";
    }

    @Override
    public boolean isSleepAfter() {
        return true;
    }
}
