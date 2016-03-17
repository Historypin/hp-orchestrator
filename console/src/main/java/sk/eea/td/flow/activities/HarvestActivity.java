package sk.eea.td.flow.activities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.model.OaipmhConfigWrapper;
import sk.eea.td.rest.service.EuropeanaHarvestService;
import sk.eea.td.rest.service.HistorypinHarvestService;
import sk.eea.td.rest.service.OaipmhHarvestService;

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


            switch (context.getJob().getSource()) {
                case EUROPEANA:
                    europeanaHarvestService.harvest(paramMap.get("luceneQuery"));
                    break;
                case HISTORYPIN:
                    historypinHarvestService.harvest(paramMap.get("projectSlug"));
                    break;
                case OAIPMH:
                    final OaipmhConfigWrapper configWrapper = new OaipmhConfigWrapper(paramMap.get("from"), paramMap.get("until"), paramMap.get("set"), paramMap.get("metadataPrefix"));
                    oaipmhHarvestService.harvest(configWrapper);
                    break;
                default:
                    throw new IllegalArgumentException("There is no harvester implemented for source: " + context.getJob().getSource());
            }
        } catch (Exception e) {
            throw new FlowException("Exception raised during harvest action", e);
        } finally {
            LOG.debug("Harvest activity for job ID: {} has ended.", context.getId());
        }
    }
}
