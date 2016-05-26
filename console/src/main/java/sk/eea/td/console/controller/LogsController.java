package sk.eea.td.console.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import sk.eea.td.console.form.LogRow;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.model.datatables.DataTablesInput;
import sk.eea.td.console.model.datatables.DataTablesOutput;
import sk.eea.td.console.repository.LogRepository;

import javax.validation.Valid;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static sk.eea.td.util.PageUtils.getPageable;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class LogsController {

    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS");

    @Autowired
    private LogRepository logRepository;

    @RequestMapping("/logs")
    public String logs(Model model) {
        return "logs";
    }

    @ResponseBody
    @RequestMapping(value = "/logs/get.logs", method = RequestMethod.GET)
    public DataTablesOutput<LogRow> getJobs(@Valid DataTablesInput input) {
        DataTablesOutput<LogRow> output = new DataTablesOutput<>();
        output.setDraw(input.getDraw());

        Map<DataTablesInput.SearchCriteria, String> searchMap = input.getSearch();
        Page<Log> logPage;
        if (isEmpty(searchMap.get(DataTablesInput.SearchCriteria.value))) {
            logPage = logRepository.findAll(getPageable(input));
        } else {
            Long jobRunId = Long.parseLong(searchMap.get(DataTablesInput.SearchCriteria.value));
            logPage = logRepository.findByJobRunId(jobRunId, getPageable(input));
        }

        List<LogRow> logs = new ArrayList<>();
        for (Log log : logPage) {
            logs.add(new LogRow(dateFormat.format(log.getTimestamp()), log.getJobRun().getId(), log.getLevel().toString(), log.getMessage()));
        }

        output.setData(logs);
        output.setRecordsTotal(logRepository.count());
        output.setRecordsFiltered(logPage.getTotalElements());

        return output;
    }
}
