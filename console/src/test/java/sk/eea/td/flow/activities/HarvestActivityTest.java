package sk.eea.td.flow.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import sk.eea.td.rest.service.EuropeanaHarvestService;
import sk.eea.td.rest.service.HistorypinHarvestService;
import sk.eea.td.rest.service.OaipmhHarvestService;
import sk.eea.td.util.DateUtils;

public class HarvestActivityTest {

	private static final Integer[] TIME_FIELDS = new Integer[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};

	@InjectMocks
	HarvestActivity harvestActivity = new HarvestActivity(); 
	
	@Mock
	EuropeanaHarvestService europeanaHarvestService;
	
	@Mock
	OaipmhHarvestService oaipmhHarvestService;
	
	@Mock
	HistorypinHarvestService historypinHarvestService;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testCalculateFromDate(){
		try {
			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			cal.setTime(new Date(0));
			Date date = harvestActivity.calculateFromDate(null, null);
			assertEquals(cal.getTime(), date);
		
			date = harvestActivity.calculateFromDate("2015-02-01T01:02:03Z", null);
			assertEquals(Instant.from(DateUtils.SYSTEM_TIME_FORMAT.parse("2015-02-01T01:02:03Z")), date.toInstant() );

			try{
				date = harvestActivity.calculateFromDate("2015-02-01T01:02:03Z", "2015-02-01T03:02:02+0200");
				fail("Should fail here.");
			}catch(DateTimeParseException e){
				// this is ok
			}

			try{
				date = harvestActivity.calculateFromDate("2015-02-01T01:02:03Z", "2015-02-02T01:59:59+02");
				fail("Should fail here.");
			}catch(DateTimeParseException e){
				// this is ok
			}
			try{
				date = harvestActivity.calculateFromDate("2015-02-01T01:02:03Z", "2015-02-02T02:00:05+02:00");
				fail("Should fail here.");
			}catch(DateTimeParseException e){
				// this is ok
			}

			Instant instant = Instant.now();
			date = harvestActivity.calculateFromDate(DateUtils.SYSTEM_TIME_FORMAT.format(instant), "2015-02-02T00:00:05Z");
//			cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));
			assertEquals(Date.from(instant), date);

		} catch (ParseException e) {
			fail("Nulls not covered");
		}
	}

	@Test
	public void testCalculateUntilDate(){
		try {
			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			cal.setTime(new Date());
			for(int field:Arrays.asList(HarvestActivityTest.TIME_FIELDS)){
				cal.set(field, cal.getActualMinimum(field));
			}
			cal.add(Calendar.SECOND, -1);
			Date yesterdayMidnight = cal.getTime();
			Date date = harvestActivity.calculateUntilDate(null);
			assertEquals(yesterdayMidnight, date);
		
			
			date = harvestActivity.calculateUntilDate("2015-02-01T01:02:03Z");
			assertEquals(Instant.from(DateUtils.SYSTEM_TIME_FORMAT.parse("2015-02-01T01:02:03Z")), date.toInstant());

			try{
				date = harvestActivity.calculateUntilDate("2015-02-01T03:02:03+02:00");
				fail("Should throw exception");
			}catch(DateTimeParseException e){
				// this is ok
			}

			date = harvestActivity.calculateUntilDate(DateUtils.SYSTEM_TIME_FORMAT.format(new Date().toInstant()));
			assertEquals(yesterdayMidnight, date);

			Instant yesterdaysEnd = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(1, ChronoUnit.SECONDS);
			date = harvestActivity.calculateUntilDate(DateUtils.SYSTEM_TIME_FORMAT.format(yesterdaysEnd));
			assertEquals(yesterdaysEnd, date.toInstant());

		} catch (ParseException e) {
			fail("Nulls not covered");
		}
	}
	
}