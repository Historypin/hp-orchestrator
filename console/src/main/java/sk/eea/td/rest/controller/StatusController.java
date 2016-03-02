package sk.eea.td.rest.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @ApiOperation(value = "Return 'OK' if REST API is up and running. Used for probe call.")
    @RequestMapping(value = "/api/status", method = RequestMethod.GET)
    public String status() {
        return "OK";
    }
}
