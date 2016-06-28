package sk.eea.td.flow.activities;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.springframework.stereotype.Component;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.service.MailService;

@Component
public class ApprovalSendMailActivity implements Activity {

    @Autowired
    private KeyBasedPersistenceTokenService keyBasedPersistenceTokenService;

    @Autowired
    private MailService mailService;

    @Value("${app.hostname}")
    private String hostname;

    @Value("${review.link.template}")
    private String reviewLinkTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(ApprovalSendMailActivity.class);

    @Override
    public ActivityAction execute(JobRun context) throws FlowException {

        String token = keyBasedPersistenceTokenService.allocateToken(context.getId().toString()).getKey();
        String link = MessageFormat.format(reviewLinkTemplate, hostname, token);
        LOG.info("hostname: {}, template: {}, link: {}", hostname, reviewLinkTemplate, link);

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

        return ActivityAction.CONTINUE;
    }

    @Override
    public String getName() {
        return "ApprovalSendMailActivity";
    }
}
