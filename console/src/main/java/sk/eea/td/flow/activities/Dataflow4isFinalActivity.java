package sk.eea.td.flow.activities;

import java.text.MessageFormat;
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

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.flow.FlowException;
import sk.eea.td.util.DateUtils;

public class Dataflow4isFinalActivity implements Activity {

	private static final Logger LOG = LoggerFactory.getLogger(Dataflow4isFinalActivity.class);
	
	@Override
	public ActivityAction execute(JobRun context) throws FlowException {
		Map<ParamKey,String> params = new HashMap<ParamKey, String>();
		context.getReadOnlyParams().stream().forEach(param -> params.put(param.getKey(), param.getValue()));
		String dateUntil = params.get(ParamKey.OAI_UNTIL);
		Calendar endOfYesterday = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		for(int field : Arrays.asList(new Integer[] {Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND})){
			endOfYesterday.set(field, endOfYesterday.getActualMinimum(field));
		}
		endOfYesterday.add(Calendar.SECOND,-1);
		if(dateUntil == null){
			LOG.debug(MessageFormat.format("Job {0} should continue in next cycle.", context.getJob().getId()));
			return ActivityAction.NEXT_CYCLE;
		}
		Date until;
		until =  GregorianCalendar.from(ZonedDateTime.parse(dateUntil, DateUtils.SYSTEM_TIME_FORMAT)).getTime();
		if(until.after(endOfYesterday.getTime())){
			LOG.debug(MessageFormat.format("Job {0} should continue in next cycle.", context.getJob().getId()));
			return ActivityAction.NEXT_CYCLE;
		}
		return ActivityAction.CONTINUE;
	}

	@Override
	public String getName() {
		return "dataflow4isFinalActivity";
	}

}
