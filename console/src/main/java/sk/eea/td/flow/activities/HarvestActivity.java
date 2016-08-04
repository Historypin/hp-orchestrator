package sk.eea.td.flow.activities;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.service.EuropeanaHarvestService;
import sk.eea.td.rest.service.HistorypinHarvestService;
import sk.eea.td.util.DateUtils;

public class HarvestActivity implements Activity {

	private static final Logger LOG = LoggerFactory.getLogger(HarvestActivity.class);

    @Autowired
    private EuropeanaHarvestService europeanaHarvestService;

    @Autowired
    private HistorypinHarvestService historypinHarvestService;

    @Override
    public ActivityAction execute(AbstractJobRun context) throws FlowException {
        LOG.debug("Starting harvest activity for job ID: {}", context.getId());
        try {
            final Map<ParamKey, String> paramMap = new HashMap<>();
            context.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));
//            final Path harvestPath;
            String from = paramMap.get(ParamKey.OAI_FROM);
			String until = paramMap.get(ParamKey.OAI_UNTIL);
			String lastSuccess = paramMap.get(ParamKey.LAST_SUCCESS);
			switch (context.getJob().getSource()) {
                case EUROPEANA:
                    europeanaHarvestService.harvest(
                            context, 
                            paramMap.get(ParamKey.EU_REST_QUERY), 
                            paramMap.get(ParamKey.EU_REST_FACET),
                            Connector.TAGAPP.equals(context.getJob().getTarget()));
                    break;
                case HISTORYPIN:
                    historypinHarvestService.harvest(context, paramMap.get(ParamKey.HP_PROJECT_SLUG));
                    break;
                case HISTORYPIN_ANNOTATION:
                	Date fromDate = DateUtils.calculateFromDate(from, lastSuccess);
                	Date untilDate = DateUtils.calculateUntilDate(until);
                	
            		if(fromDate.after(untilDate)){
            			LOG.info(MessageFormat.format("Not harvesting job:{0} because date 'from' is from future or today", context.getJob().getName()));
            			return ActivityAction.NEXT_CYCLE;
            		}
                	from = DateUtils.SYSTEM_TIME_FORMAT.format(fromDate.toInstant());
                	until = DateUtils.SYSTEM_TIME_FORMAT.format(untilDate.toInstant());
                	historypinHarvestService.harvestAnnotation(context,String.valueOf(context.getJob().getId()), from, until);
                	break;
                default:
                    throw new IllegalArgumentException("There is no harvester implemented for source: " + context.getJob().getSource());
            }
//            context.addReadOnlyParam(new ReadOnlyParam(ParamKey.HARVEST_PATH, harvestPath.toAbsolutePath().toString()));
        } catch (Exception e) {
            LOG.error("Harvest activity exception", e);
            throw new FlowException("Exception raised during harvest action", e);
        } finally {
            LOG.debug("Harvest activity for job ID: {} has ended.", context.getId());
        }
        return ActivityAction.CONTINUE;
    }

    @Override
    public String getName() {
        return "Harvest activity";
    }
}
