package sk.eea.td.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.eea.td.rest.model.HarvestResponse;

@RestController
public class HarvestController {

    @RequestMapping("/api/harvest")
    public HarvestResponse harvest() {
        return new HarvestResponse("OK");
    }

}
