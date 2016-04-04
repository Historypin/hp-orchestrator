package sk.eea.td.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static sk.eea.td.util.DateUtils.parseHistoryPinDate;

public class DateUtilsTest {

    @Test
    public void testParseHistoryPinDate() throws Exception {
        assertThat(parseHistoryPinDate("2012-11-10"), is(equalTo("2012-11-10")));

        assertThat(parseHistoryPinDate("2012-2014"), is(equalTo("2012-2014")));
        assertThat(parseHistoryPinDate("2012/2014"), is(equalTo("2012-2014")));

        assertThat(parseHistoryPinDate("2012"), is(equalTo("2012")));
        assertThat(parseHistoryPinDate("2012-31-31"), is(equalTo("2012")));
        assertThat(parseHistoryPinDate("10.2.2000"), is(equalTo("2000")));

        LocalDate now = LocalDate.now();
        assertThat(parseHistoryPinDate("blah"), is(equalTo(now.format(DateTimeFormatter.ISO_LOCAL_DATE))));
        assertThat(parseHistoryPinDate(""), is(equalTo(now.format(DateTimeFormatter.ISO_LOCAL_DATE))));
        assertThat(parseHistoryPinDate(null), is(equalTo(now.format(DateTimeFormatter.ISO_LOCAL_DATE))));
    }
}