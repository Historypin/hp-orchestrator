package sk.eea.td.flow.activities;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import sk.eea.td.console.model.*;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.flow.FlowException;
import sk.eea.td.flow.HarvestResponse;
import sk.eea.td.rest.service.EuropeanaHarvestService;
import sk.eea.td.rest.service.HistorypinHarvestService;
import sk.eea.td.rest.service.TagappHarvestService;
import sk.eea.td.util.DateUtils;
import sk.eea.td.util.ParamUtils;

public class HarvestActivity implements Activity {

    private static final Integer[] TIME_FIELDS = new Integer[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};

	private static final Logger LOG = LoggerFactory.getLogger(HarvestActivity.class);

    @Autowired
    private EuropeanaHarvestService europeanaHarvestService;

    @Autowired
    private HistorypinHarvestService historypinHarvestService;

    @Autowired
    private TagappHarvestService tagappHarvestService;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired
    private LogRepository logRepository;

    @Override
    public ActivityAction execute(JobRun context) throws FlowException {
        LOG.debug("Starting harvest activity for job ID: {}", context.getId());
        try {
            final Map<ParamKey, String> stringParamMap = ParamUtils.copyStringReadOnLyParamsIntoStringParamMap(context.getReadOnlyParams());
            final Map<ParamKey, BlobReadOnlyParam> blobParamMap = ParamUtils.copyBlobReadOnlyParamsBlobParamMap(context.getReadOnlyParams());
            final HarvestResponse harvestResponse;
            String from = stringParamMap.get(ParamKey.OAI_FROM);
			String until = stringParamMap.get(ParamKey.OAI_UNTIL);
			String lastSuccess = stringParamMap.get(ParamKey.LAST_SUCCESS);
			switch (context.getJob().getSource()) {
                case EUROPEANA:
                    harvestResponse = europeanaHarvestService.harvest(context,
                            stringParamMap,
                            blobParamMap,
                            Connector.TAGAPP.equals(context.getJob().getTarget()));
                    break;
                case HISTORYPIN:
                    harvestResponse = historypinHarvestService.harvest(String.valueOf(context.getId()), stringParamMap.get(ParamKey.HP_PROJECT_SLUG));
                    break;
                case HISTORYPIN_ANNOTATION:
                	Date fromDate = calculateFromDate(from, lastSuccess);
                	Date untilDate = calculateUntilDate(until);
                	
            		if(fromDate.after(untilDate)){
            			LOG.info(MessageFormat.format("Not harvesting job:{0} because date 'from' is from future or today", context.getJob().getName()));
            			return ActivityAction.NEXT_CYCLE;
            		}
                	from = DateUtils.SYSTEM_TIME_FORMAT.format(fromDate.toInstant());
                	until = DateUtils.SYSTEM_TIME_FORMAT.format(untilDate.toInstant());
                    harvestResponse = historypinHarvestService.harvestAnnotation(String.valueOf(context.getId()),String.valueOf(context.getJob().getId()), from, until);
                	break;
                case TAGAPP:
                    Date fromDateTA = calculateFromDate(from, lastSuccess);
                    Date untilDateTA = calculateUntilDate(until);

                    if(fromDateTA.after(untilDateTA)){
                        LOG.info(MessageFormat.format("Not harvesting job:{0} because date 'from' is from future or today", context.getJob().getName()));
                        return ActivityAction.NEXT_CYCLE;
                    }
                    from = DateUtils.SYSTEM_TIME_FORMAT.format(fromDateTA.toInstant());
                    until = DateUtils.SYSTEM_TIME_FORMAT.format(untilDateTA.toInstant());
                    harvestResponse = tagappHarvestService.harvest(String.valueOf(context.getId()),String.valueOf(context.getJob().getId()), from, until);
                    break;
                default:
                    throw new IllegalArgumentException("There is no harvester implemented for source: " + context.getJob().getSource());
            }
            context.addReadOnlyParam(new StringReadOnlyParam(ParamKey.HARVEST_PATH, harvestResponse.getHarvestPath().toAbsolutePath().toString()));
            if(!harvestResponse.isAllItemHarvested()) {
                Log log = new Log();
                log.setJobRun(context);
                log.setLevel(Log.LogLevel.ERROR);
                log.setMessage("Not all items were harvested successfully. See server logs for details.");
                logRepository.save(log);
            }
        } catch (Exception e) {
            LOG.error("Harvest activity exception", e);
            throw new FlowException("Exception raised during harvest action", e);
        } finally {
            LOG.debug("Harvest activity for job ID: {} has ended.", context.getId());
        }
        return ActivityAction.CONTINUE;
    }

	public Date calculateUntilDate(String until) throws ParseException {
		Date untilDate = until == null ? new Date() : parseDate(until);
		GregorianCalendar todayCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		floorTime(todayCalendar);
		todayCalendar.set(Calendar.SECOND, -1);
		untilDate = untilDate.before(todayCalendar.getTime()) ? untilDate : todayCalendar.getTime();
		return untilDate;
	}

	public Date calculateFromDate(String from, String lastSuccess) throws ParseException {
		Calendar fromDate = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		fromDate.setTime(from == null ? new Date(0) : parseDate(from));
		GregorianCalendar lastRunCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		lastRunCalendar.setTime(lastSuccess == null ? new Date(0) : parseDate(lastSuccess));
		floorTime(lastRunCalendar);
		fromDate = fromDate.after(lastRunCalendar) ? fromDate : lastRunCalendar;
		return fromDate.getTime();
	}

	public Date parseDate(String from) {
		return Date.from(Instant.from(DateUtils.SYSTEM_TIME_FORMAT.parse(from)));
	}
	
	public void floorTime(Calendar calendar){
		for(int field: Arrays.asList(TIME_FIELDS)){
			calendar.set(field, calendar.getActualMinimum(field));
		}
	}

    @Override
    public String getName() {
        return "Harvest activity";
    }
}
