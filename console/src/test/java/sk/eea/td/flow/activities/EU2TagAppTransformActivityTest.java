package sk.eea.td.flow.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EU2TagAppTransformActivityTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testFindValue() {
        EU2TagAppTransformActivity test = new EU2TagAppTransformActivity();
        try {
            JsonNode json = objectMapper.readTree(Paths.get(ClassLoader.getSystemResource("europeana/sample.eu.json").toURI()).toFile());
            assertEquals("Leonardo da Vinci", test.findValue(json, "items[0].edmAgentLabel[0].def"));
            assertEquals("http://purl.ox.ac.uk/uuid/7172f2ca594945638083c6d2ed6e1d8c", test.findValue(json, "items[9].edmIsShownBy[0]"));
            assertEquals("/9200143/BibliographicResource_2000069449610", test.findValue(json, "items[9].id"));
            assertEquals("http://www.europeana.eu/api/api2demo/redirect?shownAt=http%3A%2F%2Fwww.mdz-nbn-resolving.de%2Furn%2Fresolver.pl%3Furn%3Durn%3Anbn%3Ade%3Abvb%3A12-bsb10062732-1&provider=The+European+Library&id=http%3A%2F%2Fwww.europeana.eu%2Fresolve%2Frecord%2F9200386%2FBibliographicResource_3000044747440&profile=rich", test.findValue(json, "items[8].edmIsShownAt[0]"));
            assertEquals("Neue Ausg. gehalten von Anton Joseph Binterim. [[Illustr.:] L[eonardo da Vinci ; E[duard] Schuler] Besitzer: München, Bayerische Staatsbibliothek -- H.eccl. 3203 c#Beibd.1", test.findValue(json, "items[4].dcDescriptionLangAware.def"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown.");
        }
    }

}
