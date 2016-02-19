package sk.eea.td.console.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @Value("${spring.profiles.active:null}") String profile;

    @Value("${some.default.property}") String defaultExample;

    @Value("${database.url}") String databaseURL;

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("profile", profile);
        model.addAttribute("defaultExample", defaultExample);
        model.addAttribute("databaseURL", databaseURL);
        return "index";
    }
}
