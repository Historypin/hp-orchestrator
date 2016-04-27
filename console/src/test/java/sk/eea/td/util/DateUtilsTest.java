package sk.eea.td.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static sk.eea.td.util.DateUtils.isHistorypinDateValid;
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

        assertThat(parseHistoryPinDate("blah"), is(equalTo("")));
        assertThat(parseHistoryPinDate(""), is(equalTo("")));
        assertThat(parseHistoryPinDate(null), is(equalTo("")));
    }

    @Test
    public void testIsHistorypinDateValid() throws Exception {
        assertThat(isHistorypinDateValid("2012-11-10"), is(equalTo(true)));

        assertThat(isHistorypinDateValid("2012-2014"), is(equalTo(true)));
        assertThat(isHistorypinDateValid("2012/2014"), is(equalTo(true)));

        assertThat(isHistorypinDateValid("2012"), is(equalTo(true)));
        assertThat(isHistorypinDateValid("2012-31-31"), is(equalTo(false)));
        assertThat(isHistorypinDateValid("10.2.2000"), is(equalTo(false)));
        assertThat(isHistorypinDateValid("1.1.1999"), is(equalTo(false)));

        assertThat(isHistorypinDateValid("blah"), is(equalTo(false)));
        assertThat(isHistorypinDateValid(""), is(equalTo(false)));
        assertThat(isHistorypinDateValid(null), is(equalTo(false)));
    }
}
