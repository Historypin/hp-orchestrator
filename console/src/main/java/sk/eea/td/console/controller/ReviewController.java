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
@SessionAttributes("jobRunId")
public class ReviewController {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewController.class);

    @Value("${token.days.valid}")
    private Integer tokenDaysValid;

    @Autowired
    private KeyBasedPersistenceTokenService keyBasedPersistenceTokenService;

    @Autowired
    private JobRunRepository jobRunRepository;

    public List<ApproveItem> populateApproveItems() {
        List<ApproveItem> list = new ArrayList<>();

        ApproveItem item = new ApproveItem();
        item.setName("tin/chocolate");
        item.setDescription("Octagonal tin with push-on lid. Mottled blue sides. Lid printed in black and white with heavily retouched portrait of \"T.R.H PRINCESS ELIZABETH AND PRINCESS MARGARET ROSE\", \"FROM A PORTRAIT BY MARCUS ADAMS\". The image is dated \"1936\". An octagonal printed paper label showing a photograph of the contents is fixed to the base. A torn section reveals printing on the base.\n");
        item.setLinkTo("/en/ontotext-test-collection/pin/657755");
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("tin", null));
        tags.add(new Tag("chocolate", null));
        item.setTags(tags);
        list.add(item);

        item = new ApproveItem();
        item.setName("tea caddy: \"1910 Silver Jubilee 1935\".");
        item.setDescription("Commemorating the silver jubilee of George V & Queen Mary (1910-1935). Tall column, square in section. Scenes of Windsor Castle on sides. Front & back marked \"tea\" & \"1910 Silver Jubilee 1935\". From home of J.W. Bower, 1 Gedling Road, Arnold, Nottingham\n");
        item.setLinkTo("/en/ontotext-test-collection/pin/657756");
        tags = new ArrayList<>();
        tags.add(new Tag("tea", null));
        tags.add(new Tag("silver", null));
        tags.add(new Tag("George V", null));
        item.setTags(tags);
        List<Place> places = new ArrayList<>();
        places.add(new Place("1 Gedling Road", null));
        places.add(new Place("Arnold", null));
        places.add(new Place("Nottingham", null));
        item.setPlaces(places);
        list.add(item);

        item = new ApproveItem();
        item.setName("tea pot: \"Royal Silver jubilee 1910-1935\".");
        item.setDescription("Short cylindrical pressed aluminium teapot with cast spout and a black plastic loop handle. Concave taper to rim at top. Circular aluminium cover with yellow plastic knob (a replacement?) Sides embossed: \"ROYAL // SILVER JUBILEE // 1910 // 1935\" and portraits of George V in oval frames flanked by Union Jacks. Manufacturer's mark on base.\n");
        item.setLinkTo("/en/ontotext-test-collection/pin/657757");
        tags = new ArrayList<>();
        tags.add(new Tag("tea", null));
        tags.add(new Tag("pot", null));
        tags.add(new Tag("aluminium", null));
        item.setTags(tags);
        list.add(item);

        item = new ApproveItem();
        item.setName("tin/toffee: \"A souvenir of the coronation of H.M. Queen Elizabeth II\".");
        item.setDescription("Polygonal souvenir tin of Elizabeth's II coronation. Photograph of Elizabeth and Philip on lid.\n");
        item.setLinkTo("/en/ontotext-test-collection/pin/657758");
        tags = new ArrayList<>();
        tags.add(new Tag("tin", null));
        tags.add(new Tag("toffee", null));
        item.setTags(tags);
        places = new ArrayList<>();
        places.add(new Place("Londom", null));
        places.add(new Place("Abbey road", null));
        item.setPlaces(places);
        list.add(item);

        return list;
    }

    private List<String> newPopulateItems(){
        List<String> list = new ArrayList<>();
        list.add("{"
                + "  \"id\" : 14929,"
                + "  \"caption\" : \"Plummer Roddis Hastings 1927\","
                + "  \"description\" : \"Plummer Roddis store in Hastings designed by architect Henry ward ARIBA and opened in 1927 now in use as a Debenhams Store.\","
                + "  \"url\" : \"http://v77-beta-3.historypin-hrd.appspot.com/en/explore/pin/14929\","
                + "  \"original_tags\" : [ \"Cake\", \"Cooking\", \"Coffee\", \"Sponge_cake\", \"Strawberry\", \"Sugar\", \"Food\" ],"
                + "  \"approved_tags\" : [ \"Cake\", \"Cooking\", \"Coffee\", \"Sponge_cake\", \"Strawberry\", \"Sugar\", \"Food\" ]"
                + "}");

        list.add("{"
                + "  \"id\" : 14930,"
                + "  \"caption\" : \"tin/chocolate\","
                + "  \"description\" : \"Octagonal tin with push-on lid. Mottled blue sides. Lid printed in black and white with heavily retouched portrait of T.R.H PRINCESS ELIZABETH AND PRINCESS MARGARET ROSE, FROM A PORTRAIT BY MARCUS ADAMS. The image is dated 1936. An octagonal printed paper label showing a photograph of the contents is fixed to the base. A torn section reveals printing on the base.\","
                + "  \"url\" : \"http://v77-beta-3.historypin-hrd.appspot.com/en/explore/pin/14930\","
                + "  \"original_tags\" : [ \"tin\", \"Chocolate\" ],"
                + "  \"approved_tags\" : [ \"tin\", \"Chocolate\" ]"
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
        request.getSession().setAttribute("jobRunId", jobRunId);

        JobRun jobRun = jobRunRepository.findOne(jobRunId);
        if(jobRun == null || JobRun.JobRunStatus.FINISHED.equals(jobRun.getStatus())) {
            // TODO: go away
            LOG.error("Job run was not found, or has already finished.");
        }
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
        return "[" + String.join(",", items) + " ]";
    }

    @RequestMapping(value = "/review", method = RequestMethod.POST)
    public String reviewSubmit(@ModelAttribute String jobRunId) {
        LOG.debug("submit items of jobRun: {}", jobRunId);
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
