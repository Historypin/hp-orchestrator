package sk.eea.td.rest.service;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.eea.td.rest.validation.CsvFileValidationException;

import java.io.File;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class EuropeanaCsvFileValidationServiceTest {

    private static Logger LOG = LoggerFactory.getLogger(EuropeanaCsvFileValidationServiceTest.class);

    //class under test
    private EuropeanaCsvFileValidationService service;

    private static final String VALID_FILE = "europeana/valid_europeana_ids.csv";

    private static final String INVALID_FILE = "europeana/invalid_europeana_ids.csv";

    private static final String WRONG_FILE = "europeana/wrong_europeana_ids_file.csv";

    @Before
    public void setUp() throws Exception {
        service = new EuropeanaCsvFileValidationService();
    }

    @Test
    public void validFilePasses() throws Exception {
        File file = Paths.get(ClassLoader.getSystemResource(VALID_FILE).toURI()).toFile();
        try {
            service.validate(file);
        } catch (Exception e) {
            fail("Valid file must pass, but instead exception was thrown!");
            LOG.error("Thrown exception: ", e);
        }
    }

    @Test
    public void invalidFileFails() throws Exception {
        File file = Paths.get(ClassLoader.getSystemResource(INVALID_FILE).toURI()).toFile();
        try {
            service.validate(file);
            fail("In/**/valid file should have thrown an exception, instead it appears as valid!");
        } catch (Exception e) {
            assertThat(e instanceof CsvFileValidationException, is(true));
            CsvFileValidationException exception = (CsvFileValidationException) e;
            assertThat(exception.isFaultLinesOverflow(), is(false));
            assertThat(exception.getFaultLines(), containsInAnyOrder(6L, 14L));
        }
    }

    @Test
    public void wrongFileFails() throws Exception {
        File file = Paths.get(ClassLoader.getSystemResource(WRONG_FILE).toURI()).toFile();
        try {
            service.validate(file);
            fail("Invalid file should have thrown an exception, instead it appears as valid!");
        } catch (Exception e) {
            assertThat(e instanceof CsvFileValidationException, is(true));
            CsvFileValidationException exception = (CsvFileValidationException) e;
            assertThat(exception.isFaultLinesOverflow(), is(true));
            assertThat(exception.getFaultLines(), containsInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
        }
    }
}
