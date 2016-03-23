package sk.eea.td.console.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import sk.eea.td.console.form.LogRow;
import sk.eea.td.console.repository.LogRepository;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class LogsController {

    @Autowired
    private LogRepository logRepository;

    @RequestMapping("/logs")
    public String logs(Model model) {
        List<LogRow> logs = logRepository.findAllRelevantLogs().stream().map(l -> new LogRow(l.getTimestamp(), l.getJobRun().getJob().getId(), l.getLevel().toString(), l.getMessage())).collect(Collectors.toList());
        model.addAttribute("logs", logs);
        return "logs";
    }
}
