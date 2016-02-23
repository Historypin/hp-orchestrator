package sk.eea.td.rest.controller;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;
import sk.eea.td.rest.model.HarvestRequest;
import sk.eea.td.rest.model.HarvestResponse;
import sk.eea.td.rest.service.OaipmhHarvestService;

import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class HarvestController {

    private static Logger LOG = LoggerFactory.getLogger(HarvestController.class);

    @Autowired
    private OaipmhHarvestService oaipmhHarvestService;

    @ApiOperation(value = "Triggers harvest operation",
            response = HarvestResponse.class)
    @RequestMapping(value = "/api/harvest", method = RequestMethod.POST)
    public HarvestResponse harvest(@Valid @RequestBody HarvestRequest request) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        switch (request.getConnector()) {
            case EUROPEANA:
                break;
            case HISTORYPIN:
                break;
            case OAIPMH:
                executor.submit(() -> {
                    try {
                        oaipmhHarvestService.harvest(request.getOaipmhConfigWrapper());
                    } catch (NoSuchFieldException | IOException | ParserConfigurationException | SAXException | TransformerException e) {
                        LOG.error("Exception at OAI-PMH harvest job.", e);
                    }
                });
                break;
            default:
                throw new IllegalStateException("Connector :" + request.getConnector() + " is not implemented yet!");
        }

        return new HarvestResponse("EXECUTION STARTED");
    }
}
