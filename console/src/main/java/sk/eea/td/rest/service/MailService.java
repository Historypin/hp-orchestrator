package sk.eea.td.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Map;

@Component
public class MailService {

    @Value("${mail.from}")
    private String mailFrom;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends mail using 'report' template.
     * Required params:
     * <table>
     * <tr>
     * <th>Key</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>userName</td>
     * <td>Name of user.</td>
     * </tr>
     * <tr>
     * <td>taskName</td>
     * <td>Name of task.</td>
     * </tr>
     * <tr>
     * <td>taskRunId</td>
     * <td>ID of JobRun.</td>
     * </tr>
     * </table>
     * Optional params:
     * <table>
     * <tr>
     * <th>Key</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>errors</td>
     * <td>possible values: true | false. Indicates if log message of type 'error' is present for JobRun.</td>
     * </tr>
     * </table>
     *
     * @param email   recipient email address
     * @param subject subject of email message
     * @param params  map of parameters used in template
     */
    public void sendReportMail(String email, String subject, Map<String, String> params) {
        this.sendMail(email, "report", subject, params);
    }

    /**
     * Sends mail using 'approval' template.
     * Required params:
     * <table>
     * <tr>
     * <th>Key</th>
     * <th>Value</th>
     * </tr>
     * </table>
     * Optional params:
     * <table>
     * <tr>
     * <th>Key</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td></td>
     * <td></td>
     * </tr>
     * </table>
     *
     * @param email   recipient email address
     * @param subject subject of email message
     * @param params  map of parameters used in template
     */
    public void sendReviewMail(String email, String subject, Map<String, String> params) {
        this.sendMail(email, "review", subject, params);
    }

    /**
     * Sends mail using 'error' template.
     Required params:
     * <table>
     * <tr>
     * <th>Key</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>userName</td>
     * <td>Name of user.</td>
     * </tr>
     * <tr>
     * <td>taskName</td>
     * <td>Name of task.</td>
     * </tr>
     * <tr>
     * <td>taskRunId</td>
     * <td>ID of JobRun.</td>
     * </tr>
     * </table>
     * Optional params:
     * <table>
     * <tr>
     * <th>Key</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td></td>
     * <td></td>
     * </tr>
     * </table>
     *
     * @param email   recipient email address
     * @param subject subject of email message
     * @param params  map of parameters used in template
     */
    public void sendErrorMail(String email, String subject, Map<String, String> params) {
        this.sendMail(email, "error", subject, params);
    }
    /**
     * Sends mail using given template name.
     *
     * @param email        recipient email address
     * @param templateName name of template to use
     * @param subject      subject of email message
     * @param params       map of parameters used in template
     */
    public void sendMail(String email, String templateName, String subject, Map<String, String> params) {
        // Prepare the evaluation context
        final Context ctx = new Context(Locale.US);
        for (String key : params.keySet()) {
            ctx.setVariable(key, params.get(key));
        }

        // Create the HTML body using Thymeleaf
        final String htmlContent = this.templateEngine.process(templateName, ctx);

        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        try {
            message.setSubject(subject);
            message.setFrom(mailFrom);
            message.setTo(email);

            message.setText(htmlContent, true); // true = isHtml
        } catch (MessagingException ex) {
            throw new RuntimeException("Exception at sending email: ", ex);
        }

        // Send mail
        this.mailSender.send(mimeMessage);
    }
}
