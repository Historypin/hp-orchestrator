package sk.eea.td.flow.activities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.model.OaipmhConfigWrapper;
import sk.eea.td.rest.service.EuropeanaHarvestService;
import sk.eea.td.rest.service.HistorypinHarvestService;
import sk.eea.td.rest.service.OaipmhHarvestService;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HarvestActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestActivity.class);

    @Autowired
    private EuropeanaHarvestService europeanaHarvestService;

    @Autowired
    private OaipmhHarvestService oaipmhHarvestService;

    @Autowired
    private HistorypinHarvestService historypinHarvestService;

    @Override
    public void execute(JobRun context) throws FlowException {
        LOG.debug("Starting harvest activity for job ID: {}", context.getId());
        try {
            final Map<String, String> paramMap = new HashMap<>();
            context.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));
            final Path harvestPath;
            switch (context.getJob().getSource()) {
                case EUROPEANA:
                    harvestPath = europeanaHarvestService.harvest(String.valueOf(context.getId()), paramMap.get("luceneQuery"));
                    break;
                case HISTORYPIN:
                    harvestPath = historypinHarvestService.harvest(String.valueOf(context.getId()), paramMap.get("projectSlug"));
                    break;
                case OAIPMH:
                    final OaipmhConfigWrapper configWrapper = new OaipmhConfigWrapper(paramMap.get("from"), paramMap.get("until"), paramMap.get("set"), paramMap.get("metadataPrefix"));
                    harvestPath = oaipmhHarvestService.harvest(String.valueOf(context.getId()), configWrapper);
                    break;
                default:
                    throw new IllegalArgumentException("There is no harvester implemented for source: " + context.getJob().getSource());
            }
            context.addReadOnlyParam(new ReadOnlyParam("harvestPath", harvestPath.toAbsolutePath().toString()));
        } catch (Exception e) {
            throw new FlowException("Exception raised during harvest action", e);
        } finally {
            LOG.debug("Harvest activity for job ID: {} has ended.", context.getId());
        }
    }

    @Override
    public String getName() {
        return "Harvest activity";
    }
}
