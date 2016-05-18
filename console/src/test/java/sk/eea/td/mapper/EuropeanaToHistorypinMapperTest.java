package sk.eea.td.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.eu_client.api.EuropeanaClient;
import sk.eea.td.hp_client.api.Location;
import sk.eea.td.rest.model.HistorypinTransformDTO;
import sk.eea.td.rest.service.PlacesCache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class EuropeanaToHistorypinMapperTest {

    private static final Logger LOG = LoggerFactory.getLogger(EuropeanaToHistorypinMapperTest.class);

    private ObjectMapper objectMapper;

    private String EUROPEANA_PROVIDER_ID = "12346";

    private String PATH_TO_SAMPLE_FILE = "europeana/sample.eu.json";

    private Path sampleFile;

    private Map<ParamKey, String> params;

    @InjectMocks
    private EuropeanaToHistorypinMapper mapper;

    @Mock
    private EuropeanaClient europeanaClient;

    @Mock
    private PlacesCache placesCache;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.objectMapper = new ObjectMapper();
        this.sampleFile = Paths.get(this.getClass().getClassLoader().getResource(PATH_TO_SAMPLE_FILE).toURI());
        this.params = new HashMap<>();
        // required params
        params.put(ParamKey.HP_LAT, "42.0");
        params.put(ParamKey.HP_LNG, "23.0");
        params.put(ParamKey.HP_RADIUS, "10000");
        params.put(ParamKey.HP_DATE, "2016");

        ReflectionTestUtils.setField(mapper, EuropeanaToHistorypinMapper.class, "europeanaProviderId", EUROPEANA_PROVIDER_ID, String.class);
        ReflectionTestUtils.setField(mapper, EuropeanaToHistorypinMapper.class, "objectMapper", objectMapper, ObjectMapper.class);

        when(europeanaClient.getRecordsEdmIsShownBy(anyString())).thenReturn("http://some.url.to.object");
        when(placesCache.getLocation(anyString())).thenReturn(new Location(42.0, 23.0, 1000L));
    }

    /**
     * Test mapping.
     *
     * Test is expected to throw 3 MissingRequiredFieldExceptions.
     *
     * @throws IOException
     */
    @Test
    public void testMap() throws IOException {
        final Path transformFile = Files.createTempFile("tmp", ".tmp");

        boolean result = mapper.map(sampleFile, transformFile, params);

        final HistorypinTransformDTO transformation = objectMapper.readValue(transformFile.toFile(), HistorypinTransformDTO.class);
        assertThat(transformation, is(not(nullValue())));
        assertThat(transformation.getPins().size(), is(equalTo(9)));
        assertThat(result, is(eq(false)));

        Files.deleteIfExists(transformFile);
    }
}
