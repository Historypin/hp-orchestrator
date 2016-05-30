package sk.eea.td.console.controller;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.springframework.security.core.token.Token;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sk.eea.td.console.form.*;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.datatables.DataTablesInput;
import sk.eea.td.console.model.datatables.DataTablesOutput;
import sk.eea.td.console.model.dto.ReviewDTO;
import sk.eea.td.console.model.dto.ReviewDTOWrapper;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.validation.BadTokenException;
import sk.eea.td.console.validation.ExecutionNotFoundException;
import sk.eea.td.service.ApprovementService;
import sk.eea.td.service.ServiceException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

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
        final JobRun jobRun = jobRunRepository.findOne(jobRunId);
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
        final JobRun jobRun = retrieveJobRunFromSession(request);

        List<ReviewDTO> reviews = approvementService.load(jobRun);
        return new ReviewDTOWrapper(reviews);
    }

    @ResponseBody
    @RequestMapping(value = "/review/save.items", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public GenericResponse saveReviewItems(@RequestBody List<ReviewDTO> reviews,  HttpServletRequest request)
            throws ServiceException {
        final JobRun jobRun = retrieveJobRunFromSession(request);

        LOG.debug("Received reviews for saving: {}", reviews);

        approvementService.save(jobRun, reviews);
        return new GenericResponse("OK");
    }

    @ResponseBody
    @RequestMapping(value = "/review/send.items", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public GenericResponse sendReviewItems(@RequestBody List<ReviewDTO> reviews, HttpServletRequest request) throws ServiceException {
        final JobRun jobRun = retrieveJobRunFromSession(request);

        approvementService.saveAndSendApproved(jobRun, reviews);
        return new GenericResponse("OK");
    }

    @ResponseBody
    @RequestMapping(value = "/review", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public GenericResponse finishReviewItems(@RequestBody List<ReviewDTO> reviews, HttpServletRequest request)
            throws ServiceException {
        final JobRun jobRun = retrieveJobRunFromSession(request);

        approvementService.save(jobRun, reviews);
        // TODO: finish task
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
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
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

    private JobRun retrieveJobRunFromSession(HttpServletRequest request) {
        final Long jobRunId = (Long) request.getSession().getAttribute("jobRunId");
        if(jobRunId == null) {
            LOG.error("Session does not contain 'jobRunId' attribute.");
            throw new BadTokenException();
        }

        final JobRun jobRun = jobRunRepository.findOne(jobRunId);
        if(jobRun == null || !JobRun.JobRunStatus.WAITING.equals(jobRun.getStatus())) {
            throw new ExecutionNotFoundException(jobRunId, "JobRun missing or not in WAITING state.");
        }
        return jobRun;
    }
}
