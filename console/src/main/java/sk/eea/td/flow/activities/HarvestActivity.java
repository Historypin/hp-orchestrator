package sk.eea.td.flow.activities;

import java.nio.file.Path;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
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

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.model.OaipmhConfigWrapper;
import sk.eea.td.rest.service.EuropeanaHarvestService;
import sk.eea.td.rest.service.HistorypinHarvestService;
import sk.eea.td.rest.service.OaipmhHarvestService;
import sk.eea.td.rest.service.TagappHarvestService;
import sk.eea.td.util.DateUtils;

public class HarvestActivity implements Activity {

    private static final Integer[] TIME_FIELDS = new Integer[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};

	private static final Logger LOG = LoggerFactory.getLogger(HarvestActivity.class);

    @Autowired
    private EuropeanaHarvestService europeanaHarvestService;

    @Autowired
    private OaipmhHarvestService oaipmhHarvestService;

    @Autowired
    private HistorypinHarvestService historypinHarvestService;

    @Autowired
    private TagappHarvestService tagappHarvestService;
    
    @Override
    public ActivityAction execute(JobRun context) throws FlowException {
        LOG.debug("Starting harvest activity for job ID: {}", context.getId());
        try {
            final Map<ParamKey, String> paramMap = new HashMap<>();
            context.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));
            final Path harvestPath;
            String from = paramMap.get(ParamKey.OAI_FROM);
			String until = paramMap.get(ParamKey.OAI_UNTIL);
			String lastSuccess = paramMap.get(ParamKey.LAST_SUCCESS);
			switch (context.getJob().getSource()) {
                case EUROPEANA:
                    harvestPath = europeanaHarvestService.harvest(String.valueOf(context.getId()), paramMap.get(ParamKey.EU_REST_QUERY), paramMap.get(ParamKey.EU_REST_FACET));
                    break;
                case HISTORYPIN:
                    harvestPath = historypinHarvestService.harvest(String.valueOf(context.getId()), paramMap.get(ParamKey.HP_PROJECT_SLUG));
                    break;
                case OAIPMH:
                    final OaipmhConfigWrapper configWrapper = new OaipmhConfigWrapper(from, until, paramMap.get(ParamKey.OAI_SET));
                    harvestPath = oaipmhHarvestService.harvest(String.valueOf(context.getId()), configWrapper);
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
                	harvestPath = historypinHarvestService.harvestAnnotation(String.valueOf(context.getId()),String.valueOf(context.getJob().getId()), from, until);
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
                    harvestPath = tagappHarvestService.harvest(String.valueOf(context.getId()),String.valueOf(context.getJob().getId()), from, until);
                    break;
                default:
                    throw new IllegalArgumentException("There is no harvester implemented for source: " + context.getJob().getSource());
            }
            context.addReadOnlyParam(new ReadOnlyParam(ParamKey.HARVEST_PATH, harvestPath.toAbsolutePath().toString()));
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
