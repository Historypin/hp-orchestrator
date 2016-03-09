package sk.eea.td.console.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController {

    @RequestMapping("/settings")
    public String settings() {
        return "settings";
    }
}
