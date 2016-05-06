package sk.eea.td.console.controller;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.springframework.security.core.token.Token;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.validation.BadTokenException;
import sk.eea.td.console.validation.ExecutionNotFoundException;

import java.util.Date;

@Controller
public class ReviewController {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewController.class);

    @Value("${token.days.valid}")
    private Integer tokenDaysValid;

    @Autowired
    private KeyBasedPersistenceTokenService keyBasedPersistenceTokenService;

    @Autowired
    private JobRunRepository jobRunRepository;

    @RequestMapping(value = "/review", method = RequestMethod.GET)
    public String review(@RequestParam(name = "token", required = false) String inputToken) {
        final Token token = keyBasedPersistenceTokenService.verifyToken(inputToken);
        if (token == null || isExpiredToken(token)) {
            throw new BadTokenException();
        }

        final Long jobRunId = Long.parseLong(token.getExtendedInformation());
        final JobRun jobRun = jobRunRepository.findOne(jobRunId);
        if (jobRun == null || JobRun.JobRunStatus.FINISHED.equals(jobRun.getStatus())) {
            throw new ExecutionNotFoundException(
                    String.format("JobRun id='%s' does not exist, or is already finished.", jobRunId));
        }
        return "review";
    }

    @ExceptionHandler({BadTokenException.class, IllegalArgumentException.class, NumberFormatException.class})
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public
    @ResponseBody
    String handleException(Exception e) {
        LOG.error("Exception thrown during processing of review token. Access to review page was denied.", e);
        return "Token is missing, not valid or already expired!";
    }

    private boolean isExpiredToken(Token token) {
        Date validUntil = DateUtils.addDays(new Date(token.getKeyCreationTime()), tokenDaysValid);
        return new Date().after(validUntil);
    }
}
