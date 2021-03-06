package sk.eea.td.console.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.springframework.security.core.token.Token;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import sk.eea.td.console.form.GenericResponse;
import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.dto.ReviewDTO;
import sk.eea.td.console.model.dto.ReviewDTOWrapper;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.validation.BadTokenException;
import sk.eea.td.console.validation.ExecutionNotFoundException;
import sk.eea.td.service.ApprovementService;
import sk.eea.td.service.ServiceException;

@Controller
public class ReviewController {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewController.class);

    @Value("${token.days.valid}")
    private Integer tokenDaysValid;

    @Autowired
    private KeyBasedPersistenceTokenService keyBasedPersistenceTokenService;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired
    private ApprovementService approvementService;


    @RequestMapping(value = "/review", method = RequestMethod.GET)
    public String review(@RequestParam(name = "token", required = false) String inputToken, HttpServletRequest request) {
        final Token token = keyBasedPersistenceTokenService.verifyToken(inputToken);
        if (token == null || isExpiredToken(token)) {
            throw new BadTokenException();
        }

        final Long jobRunId = Long.parseLong(token.getExtendedInformation());
        final AbstractJobRun jobRun = jobRunRepository.findOne(jobRunId);
        if(jobRun == null || !JobRun.JobRunStatus.WAITING.equals(jobRun.getStatus())) {
            throw new ExecutionNotFoundException(jobRunId, "JobRun missing or not in WAITING state.");
        }
        request.getSession().setAttribute("jobRunId", jobRunId);
        return "review";
    }

    @ResponseBody
    @RequestMapping(value = "/review/get.token", method = RequestMethod.GET, produces = "application/json")
    public String getToken() {
        return keyBasedPersistenceTokenService.allocateToken("402").getKey();
    }

    @ResponseBody
    @RequestMapping(value = "/review/get.items", method = RequestMethod.GET, produces = "application/json")
    public ReviewDTOWrapper getReviewItems(HttpServletRequest request) throws ServiceException {
        final AbstractJobRun jobRun = retrieveJobRunFromSession(request);

        LOG.debug("Retrieving reviews for jobRunId: {}", jobRun.getId());

        List<ReviewDTO> reviews = approvementService.load(jobRun);

        LOG.debug("Retrieved reviews: {}", reviews);

        return new ReviewDTOWrapper(reviews);
    }

    @ResponseBody
    @RequestMapping(value = "/review/save.items", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public GenericResponse saveReviewItems(@RequestBody List<ReviewDTO> reviews,  HttpServletRequest request)
            throws ServiceException {
        final AbstractJobRun jobRun = retrieveJobRunFromSession(request);

        LOG.debug("Received reviews for saving: {}", reviews);

        approvementService.save(jobRun, reviews);
        return new GenericResponse("OK");
    }

    @ResponseBody
    @RequestMapping(value = "/review/send.items", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public GenericResponse sendReviewItems(@RequestBody List<ReviewDTO> reviews, HttpServletRequest request) throws ServiceException {
        final AbstractJobRun jobRun = retrieveJobRunFromSession(request);

        LOG.debug("Received reviews for sending: {}", reviews);

        approvementService.saveAndSendApproved(jobRun, reviews, Boolean.FALSE);
        return new GenericResponse("OK");
    }

    @ResponseBody
    @RequestMapping(value = "/review/finish.items", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public GenericResponse finishReviewItems(@RequestBody List<ReviewDTO> reviews, HttpServletRequest request)
            throws ServiceException {
        final AbstractJobRun jobRun = retrieveJobRunFromSession(request);

        LOG.debug("Received reviews for finishing: {}", reviews);

        approvementService.saveAndSendApproved(jobRun, reviews, Boolean.TRUE);
        return new GenericResponse("OK");
    }

    @ExceptionHandler({BadTokenException.class, IllegalArgumentException.class, NumberFormatException.class})
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public  String handleException(Exception e) {
        LOG.error("Exception thrown during processing of review token. Access to review page was denied.", e);
        return "Token is missing, not valid or already expired!";
    }

    @ExceptionHandler(ExecutionNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public
    String handleExecutionNotFoundException(ExecutionNotFoundException e) {
        LOG.error("Execution with id: {} could not be found, or has already finished.", e.getExecutionId(), e);
        return "Execution not found or already finished!";
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public
    GenericResponse handleServiceException(ServiceException e) {
        final String errors = e.getErrors().stream().map( error -> error.getErrorCode().toString()).collect(Collectors.joining(", "));
        LOG.error("Exception occurred during review action. Error codes: {}", errors, e);
        return new GenericResponse("FAILURE", "Following errors occurred: " + errors + ". Please try again or contact the administrator.");
    }

    private boolean isExpiredToken(Token token) {
        Date validUntil = DateUtils.addDays(new Date(token.getKeyCreationTime()), tokenDaysValid);
        return new Date().after(validUntil);
    }

    private AbstractJobRun retrieveJobRunFromSession(HttpServletRequest request) {
        final Long jobRunId = (Long) request.getSession().getAttribute("jobRunId");
        if(jobRunId == null) {
            LOG.error("Session does not contain 'jobRunId' attribute.");
            throw new BadTokenException();
        }

        final AbstractJobRun jobRun = jobRunRepository.findOne(jobRunId);
        if(jobRun == null || !JobRun.JobRunStatus.WAITING.equals(jobRun.getStatus())) {
            throw new ExecutionNotFoundException(jobRunId, "JobRun missing or not in WAITING state.");
        }
        return jobRun;
    }
}
