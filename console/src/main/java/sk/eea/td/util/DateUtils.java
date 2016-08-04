package sk.eea.td.util;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {

    public static Pattern YEAR_PATTERN = Pattern.compile("^\\d{4}|\\d{4}$");

    public static Pattern YEAR_RANGE_PATTERN = Pattern.compile("(^\\d{4})[-/](\\d{4})$");

    public static DateTimeFormatter HP_CONCRETE_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    
    public static DateTimeFormatter SYSTEM_TIME_FORMAT = DateTimeFormatter.ISO_INSTANT;
    
    /**
     * Hours, minutes, seconds, milliseconds.
     */
    private static final Integer[] TRUNCATION_FIELDS = new Integer[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};
    
    /**
     * Method will try to parse input String into Historypin acceptable date format ("YYYY-MM-DD", "YYYY-YYYY", "YYYY") in best effort basis.
     *
     * @param date String to parse.
     * @return date in HP required format parsed from date, or empty string if parsing has failed.
     */
    public static String parseHistoryPinDate(String date) {
        if(isNotEmpty(date)) {
            // first try to parse concrete date
            try {
                LocalDate.parse(date, HP_CONCRETE_DATE_FORMAT);
                return date;
            } catch (DateTimeParseException e) {
                // let it pass
            }

            // second try to parse range pattern
            Matcher matcher = YEAR_RANGE_PATTERN.matcher(date);
            if (matcher.find()) {
                return String.format("%s-%s", matcher.group(1), matcher.group(2));
            }

            // finally try to find at least year
            matcher = YEAR_PATTERN.matcher(date);
            if (matcher.find()) {
                return matcher.group();
            }
        }

        return "";
    }

    /**
     * Method validates input string if it contains date at least in one of theses formats:
     * <ul>
     *     <li>YYYY</li>
     *     <li>YYYY-YYYY</li>
     *     <li>YYYY-MM-DD</li>
     * </ul>
     *
     * @param date Input date
     * @return true if string is in correct form
     */
    public static boolean isHistorypinDateValid(String date) {
        if(isEmpty(date)) {
            return false;
        }

        try {
            LocalDate.parse(date, HP_CONCRETE_DATE_FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            // let it pass
        }

        return YEAR_RANGE_PATTERN.matcher(date).matches() || YEAR_PATTERN.matcher(date).matches();
    }
	
    /**
     * Parse string formatted in ISO format, and returns last success date or earliest date if last success is null.
     * @param from
     * @param lastSuccess
     * @return
     * @throws ParseException
     */
    public static Date calculateFromDate(String from, String lastSuccess) throws ParseException {
        Calendar fromDate = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        fromDate.setTime(from == null ? new Date(0) : parseDate(from));
        GregorianCalendar lastRunCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        lastRunCalendar.setTime(lastSuccess == null ? new Date(0) : parseDate(lastSuccess));
        floorTime(lastRunCalendar);
        fromDate = fromDate.after(lastRunCalendar) ? fromDate : lastRunCalendar;
        return fromDate.getTime();
    }

    /**
     * Parses ISO-8601 date.
     * @param from
     * @return
     */
    public static Date parseDate(String from) {
        return Date.from(Instant.from(DateUtils.SYSTEM_TIME_FORMAT.parse(from)));
    }
    
    /**
     * Set date {@link DateUtils#TRUNCATION_FIELDS} to 0.
     * @param calendar
     */
    public static void floorTime(Calendar calendar){
        for(int field: Arrays.asList(TRUNCATION_FIELDS)){
            calendar.set(field, calendar.getActualMinimum(field));
        }
    }
    /**
     * Parse string formatted in ISO format, and returns until date or today date if until is after today.
     * @param from
     * @param lastSuccess
     * @return
     * @throws ParseException
     */    
    public static Date calculateUntilDate(String until) throws ParseException {
        Date untilDate = until == null ? new Date() : parseDate(until);
        GregorianCalendar todayCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        floorTime(todayCalendar);
        todayCalendar.set(Calendar.SECOND, -1);
        untilDate = untilDate.before(todayCalendar.getTime()) ? untilDate : todayCalendar.getTime();
        return untilDate;
    }
    
}
