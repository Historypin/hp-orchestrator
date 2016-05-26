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
import sk.eea.td.console.model.datatables.DataTablesInput;
import sk.eea.td.console.model.datatables.DataTablesOutput;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.validation.BadTokenException;
import sk.eea.td.console.validation.ExecutionNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    private List<String> newPopulateItems(){
        List<String> list = new ArrayList<>();
        list.add("{"
                + "  \"id\" : 14929,"
                + "  \"caption\" : \"Plummer Roddis Hastings 1927\","
                + "  \"description\" : \"Plummer Roddis store in Hastings designed by architect Henry ward ARIBA and opened in 1927 now in use as a Debenhams Store.\","
                + "  \"url\" : \"http://v77-beta-3.historypin-hrd.appspot.com/en/explore/pin/14929\","
                + "  \"original_tags\" : [ \"Cake\", \"Cooking\", \"Coffee\", \"Sponge_cake\", \"Strawberry\", \"Sugar\", \"Food\" ],"
                + "  \"approved_tags\" : [ \"Cake\", \"Cooking\", \"Coffee\", \"Sponge_cake\", \"Strawberry\", \"Sugar\", \"Food\" ],"
                + "  \"approved\": false"
                + "}");

        list.add("{"
                + "  \"id\" : 14930,"
                + "  \"caption\" : \"tin/chocolate\","
                + "  \"description\" : \"Octagonal tin with push-on lid. Mottled blue sides. Lid printed in black and white with heavily retouched portrait of T.R.H PRINCESS ELIZABETH AND PRINCESS MARGARET ROSE, FROM A PORTRAIT BY MARCUS ADAMS. The image is dated 1936. An octagonal printed paper label showing a photograph of the contents is fixed to the base. A torn section reveals printing on the base.\","
                + "  \"url\" : \"http://v77-beta-3.historypin-hrd.appspot.com/en/explore/pin/14930\","
                + "  \"original_tags\" : [ \"tin\", \"Chocolate\" ],"
                + "  \"approved_tags\" : [ \"tin\", \"Chocolate\" ],"
                + "  \"approved\": true"
                + "}");

        return list;
    }

    @RequestMapping(value = "/review", method = RequestMethod.GET)
    public String review(@RequestParam(name = "token", required = false) String inputToken, HttpServletRequest request) {
        final Token token = keyBasedPersistenceTokenService.verifyToken(inputToken);
        if (token == null || isExpiredToken(token)) {
            throw new BadTokenException();
        }

        final Long jobRunId = Long.parseLong(token.getExtendedInformation());
        final JobRun jobRun = jobRunRepository.findOne(jobRunId);
        if(jobRun == null || JobRun.JobRunStatus.FINISHED.equals(jobRun.getStatus())) {
            LOG.error("JobRun was not found, or has already finished.");
            // TODO: go away
            //throw new BadTokenException();
        }
        request.getSession().setAttribute("jobRunId", jobRunId);
        return "review";
    }

    @ResponseBody
    @RequestMapping(value = "/review/get.items", method = RequestMethod.GET, produces = "application/json")
    public String getReviewItems(HttpServletRequest request) {
        final Long jobRunId = (Long) request.getSession().getAttribute("jobRunId");
        if(jobRunId == null) {
            throw new BadTokenException();
        }
        LOG.debug("get items from jobRun: {}", jobRunId);

        List<String> items = newPopulateItems(); // TODO: call Mano's service
        return "{\"reviews\": [" + String.join(",", items) + " ]}";
    }

    @ResponseBody
    @RequestMapping(value = "/review/save.items", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public GenericResponse saveReviewItems(@RequestBody String reviews,  HttpServletRequest request) {
        final Long jobRunId = (Long) request.getSession().getAttribute("jobRunId");
        if(jobRunId == null) {
            throw new BadTokenException();
        }

        LOG.debug("saving items of jobRun: {}", jobRunId);
        LOG.debug("received reviews: {}", reviews);
        return new GenericResponse("OK");
    }

    @ResponseBody
    @RequestMapping(value = "/review/send.items", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public String sendReviewItems(HttpServletRequest request) {
        final Long jobRunId = (Long) request.getSession().getAttribute("jobRunId");
        if(jobRunId == null) {
            throw new BadTokenException();
        }
        LOG.debug("sending approved items of jobRun: {}", jobRunId);
        return "review";
    }

    @ResponseBody
    @RequestMapping(value = "/review", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public String finishReviewItems(HttpServletRequest request) {
        final Long jobRunId = (Long) request.getSession().getAttribute("jobRunId");
        if(jobRunId == null) {
            throw new BadTokenException();
        }
        LOG.debug("finishing of jobRun: {}", jobRunId);
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

    @ExceptionHandler(ExecutionNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public
    @ResponseBody
    String handleExecutionNotFoundException(Exception e) {
        LOG.error("Exception thrown during processing of review token. Access to review page was denied.", e);
        return e.getMessage();
    }

    private boolean isExpiredToken(Token token) {
        Date validUntil = DateUtils.addDays(new Date(token.getKeyCreationTime()), tokenDaysValid);
        return new Date().after(validUntil);
    }
}
