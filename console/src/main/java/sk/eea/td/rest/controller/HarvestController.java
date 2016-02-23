package sk.eea.td.rest.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sk.eea.td.rest.model.HarvestRequest;
import sk.eea.td.rest.model.HarvestResponse;

import javax.validation.Valid;

@RestController
public class HarvestController {

    @ApiOperation(value = "Triggers harvest operation",
            response = HarvestResponse.class)
    @RequestMapping(value = "/api/harvest", method = RequestMethod.POST)
    public HarvestResponse harvest(@Valid @RequestBody HarvestRequest request) {
        switch (request.getConnector()) {
            case EUROPEANA:
                break;
            case HISTORYPIN:
                break;
            case OAIPMH:
                break;
            default:
                throw new IllegalStateException("Connector :" + request.getConnector() + " is not implemented yet!");
        }

        return new HarvestResponse("OK");
    }
}
