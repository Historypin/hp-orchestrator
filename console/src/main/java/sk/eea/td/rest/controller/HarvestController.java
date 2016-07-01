package sk.eea.td.rest.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.validation.Valid;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import sk.eea.td.rest.model.HarvestRequest;
import sk.eea.td.rest.model.HarvestResponse;
import sk.eea.td.rest.service.EuropeanaHarvestService;
import sk.eea.td.rest.service.HistorypinHarvestService;
import sk.eea.td.rest.service.OaipmhHarvestService;

@RestController
public class HarvestController {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestController.class);

    private static final ExecutorService oaipmhExecutorService = Executors.newSingleThreadExecutor();

    private static final ExecutorService historypinExecutorService = Executors.newSingleThreadExecutor();

    private static final ExecutorService europeanaExecutorService = Executors.newSingleThreadExecutor();

    @Autowired
    private OaipmhHarvestService oaipmhHarvestService;

    @Autowired
    private HistorypinHarvestService historypinHarvestService;

    @Autowired
    private EuropeanaHarvestService europeanaHarvestService;

    @ApiOperation(value = "Triggers harvest operation",
            response = HarvestResponse.class)
    @RequestMapping(value = "/api/harvest", method = RequestMethod.POST)
    public HarvestResponse harvest(@Valid @RequestBody HarvestRequest request) {
        switch (request.getConnector()) {
            case EUROPEANA:
                europeanaExecutorService.submit(() -> {
                    try {
                        europeanaHarvestService.harvest(String.valueOf(System.currentTimeMillis()), request.getLuceneQuery(), request.getSearchFacet(),Boolean.FALSE);
                    } catch (IOException | InterruptedException e) {
                        LOG.error("Exception at Europeana harvest job.", e);
                    }
                });
                break;
            case HISTORYPIN:
                historypinExecutorService.submit(() -> {
                    try {
                        historypinHarvestService.harvest(String.valueOf(System.currentTimeMillis()), request.getProjectSlug());
                    } catch (IOException |ParseException e) {
                        LOG.error("Exception at Historypin harvest job.", e);
                    }
                });
                break;
            default:
                throw new IllegalStateException("Connector :" + request.getConnector() + " is not implemented yet!");
        }

        return new HarvestResponse("EXECUTION STARTED");
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        List<ExecutorService> executors = Arrays.asList(oaipmhExecutorService, historypinExecutorService, europeanaExecutorService);
        for (ExecutorService executor : executors) {
            executor.shutdown();
            if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                LOG.error("Executor did not terminate in the specified time.");
                List<Runnable> droppedTasks = executor.shutdownNow();
                LOG.error("Executor was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed.");
            }
        }
    }
}
