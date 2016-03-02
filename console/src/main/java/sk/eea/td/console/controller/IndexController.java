package sk.eea.td.console.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class IndexController {

    @Value("${spring.profiles.active:null}") String profile;

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("profile", profile);
        return "index";
    }
}
