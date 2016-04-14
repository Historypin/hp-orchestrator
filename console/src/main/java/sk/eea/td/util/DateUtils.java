package sk.eea.td.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class DateUtils {

    public static Pattern YEAR_PATTERN = Pattern.compile("^\\d{4}|\\d{4}$");

    public static Pattern YEAR_RANGE_PATTERN = Pattern.compile("(^\\d{4})[-/](\\d{4})$");

    public static DateTimeFormatter HP_CONCRETE_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

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
}
