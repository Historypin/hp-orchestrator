package sk.eea.td.flow.activities;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.service.TagappHarvestService;
import sk.eea.td.util.DateUtils;
import sk.eea.td.util.ParamUtils;

public class TagappHarvestActivity implements Activity {
    
    private static final Logger LOG = LoggerFactory.getLogger(TagappHarvestActivity.class);
    
    @Autowired
    private TagappHarvestService tagappHarvestService;

    @Override
    public ActivityAction execute(AbstractJobRun context) throws FlowException {
        LOG.debug("Starting harvest activity for job ID: {}", context.getId());
        try {
            final Map<ParamKey, String> paramMap = ParamUtils.copyStringReadOnLyParamsIntoStringParamMap(context.getReadOnlyParams());
            String from = paramMap.get(ParamKey.OAI_FROM);
            String until = paramMap.get(ParamKey.OAI_UNTIL);
            String lastSuccess = paramMap.get(ParamKey.LAST_SUCCESS);

            String batchId = paramMap.get(ParamKey.TAGAPP_BATCH);
            Date fromDateTA = DateUtils.calculateFromDate(from, lastSuccess);
            Date untilDateTA = DateUtils.calculateUntilDate(until);

            if(fromDateTA.after(untilDateTA)){
                LOG.info(MessageFormat.format("Not harvesting job:{0} because date 'from' is from future or today", context.getJob().getName()));
                return ActivityAction.NEXT_CYCLE;
            }
            from = DateUtils.SYSTEM_TIME_FORMAT.format(fromDateTA.toInstant());
            until = DateUtils.SYSTEM_TIME_FORMAT.format(untilDateTA.toInstant());
            tagappHarvestService.harvest(context,batchId, from, until);
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
        return TagappHarvestActivity.class.getSimpleName();
    }
}
