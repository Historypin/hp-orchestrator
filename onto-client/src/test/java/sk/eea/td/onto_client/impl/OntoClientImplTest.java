package sk.eea.td.onto_client.impl;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.core.JsonParseException;

import sk.eea.td.onto_client.dto.ExtractResponseDTO;

@PrepareForTest({ OntoClientImpl.class })
@RunWith(PowerMockRunner.class)
public class OntoClientImplTest {

    private Response response;

    private static final String EXTRACT_RESPONSE = "{\"mentions\":[{\"name\":\"London\",\"startOffset\":28,\"endOffset\":34,\"type\":\"Location\",\"features\":{\"inst\":\"http://ontology.ontotext.com/resource/tsk6znpfj4e8\",\"class\":\"http://ontology.ontotext.com/taxonomy/Location\",\"confidence\":0.954509424862664,\"relevanceScore\":0.49999999999999994,\"isTrusted\":\"true\"}},{\"name\":\"UK.\",\"startOffset\":47,\"endOffset\":50,\"type\":\"Location\",\"features\":{\"inst\":\"http://ontology.ontotext.com/resource/tsk7r28yui9s\",\"class\":\"http://ontology.ontotext.com/taxonomy/Location\",\"confidence\":0.9978370854256953,\"relevanceScore\":0.08695652173913049,\"isTrusted\":\"true\"}},{\"name\":\"text\",\"startOffset\":5,\"endOffset\":9,\"type\":\"Keyphrase\",\"features\":{\"class\":\"http://ontology.ontotext.com/taxonomy/Keyphrase\",\"relevanceScore\":1,\"confidence\":0.5,\"inst\":\"http://data.ontotext.com/publishing/topic/Text\",\"isGenerated\":\"true\"}}]}";

    private static final String EXTRACT_RESPONSE_WITHOUT_LOCATION = "{\"mentions\":[{\"name\":\"text\",\"startOffset\":5,\"endOffset\":9,\"type\":\"Keyphrase\",\"features\":{\"class\":\"http://ontology.ontotext.com/taxonomy/Keyphrase\",\"relevanceScore\":1,\"confidence\":0.5,\"inst\":\"http://data.ontotext.com/publishing/topic/Text\",\"isGenerated\":\"true\"}}]}";

    private static final String CONCEPT_RESPONSE = "{\"uri\":\"http://ontology.ontotext.com/resource/tsk6znpfj4e8\",\"preferredLabel\":\"London\",\"shortDescription\":\"capital city of England and the United Kingdom\",\"description\":\"London (/ˈlʌndən/) is the capital city of England and the United Kingdom. It is the most populous city in the United Kingdom, with a metropolitan area of over 13 million inhabitants. Standing on the River Thames, London has been a major settlement for two millennia, its history going back to its founding by the Romans, who named it Londinium. London's ancient core, the City of London, largely retains its 1.12-square-mile (2.9 km2) mediaeval boundaries and in 2011 had a resident population of 7,375, making it the smallest city in England. Since at least the 19th century, the term London has also referred to the metropolis developed around this core. The bulk of this conurbation forms the Greater London administrative area (coterminous with the London region), governed by the Mayor of London and the London Assembly.London is a leading global city, with strengths in the arts, commerce, education, entertainment, fashion, finance, healthcare, media, professional services, research and development, tourism, and transport all contributing to its preeminence. It is one of the world's leading financial centres and has the fifth-or sixth-largest metropolitan area GDP in the world depending on measurement. London is a world cultural capital. It is the world's most-visited city as measured by international arrivals and has the world's largest city airport system measured by passenger traffic. London's 43 universities form the largest concentration of higher education institutes in Europe. In 2012, London became the first city to host the modern Summer Olympic Games three times.London has a diverse range of peoples and cultures, and more than 300 languages are spoken within Greater London. The region had an official population of 8,416,535 in 2013, making it the most populous municipality in the European Union, and accounting for 12.5% of the UK population. London's urban area is the second-largest in the EU with a population of 9,787,426 according to the 2011 census. London's metropolitan area is the largest in the EU with a total population of 13,614,409, while the Greater London Authority puts the population of London metropolitan region at 21 million.London was the world's most populous city from around 1831 to 1925.London contains four World Heritage Sites: the Tower of London; Kew Gardens; the site comprising the Palace of Westminster, Westminster Abbey, and St Margaret's Church; and the historic settlement of Greenwich (in which the Royal Observatory, Greenwich marks the Prime Meridian, 0° longitude, and GMT). Other famous landmarks include Buckingham Palace, the London Eye, Piccadilly Circus, St Paul's Cathedral, Tower Bridge, Trafalgar Square, and The Shard. London is home to numerous museums, galleries, libraries, sporting events and other cultural institutions, including the British Museum, National Gallery, Tate Modern, British Library and 40 West End theatres. The London Underground is the oldest underground railway network in the world.\",\"thumbnailUrl\":\"http://commons.wikimedia.org/wiki/Special:FilePath/Location_of_London_in_England_and_the_United_Kingdom_(alternative_version).svg?width=300\",\"pictureUrl\":\"http://commons.wikimedia.org/wiki/Special:FilePath/Location_of_London_in_England_and_the_United_Kingdom_(alternative_version).svg\",\"types\":[\"http://ontology.ontotext.com/taxonomy/Thing\",\"http://ontology.ontotext.com/taxonomy/Concept\",\"http://ontology.ontotext.com/taxonomy/Location\",\"http://ontology.ontotext.com/taxonomy/PopulatedPlace\"],\"exactMatch\":[\"http://dbpedia.org/resource/London\",\"http://www.wikidata.org/entity/Q84\",\"http://sws.geonames.org/2643743/\"],\"directType\":[\"http://ontology.ontotext.com/taxonomy/PopulatedPlace\"],\"coordinate location\":[{\"hasValue\":{\"label\":\"51.507222222222°N 0.1275°W\"}}],\"located in\":[{\"hasValue\":{\"uri\":\"http://ontology.ontotext.com/resource/tsk56plmf4zk\",\"label\":\"Greater London\"}}],\"head of goverment\":[{\"hasValue\":{\"uri\":\"http://ontology.ontotext.com/resource/tsk4ypj1t4hs\",\"label\":\"Boris Johnson\"}},{\"hasValue\":{\"uri\":\"http://ontology.ontotext.com/resource/tsk6g5adkk5c\",\"label\":\"Ken Livingstone\"}}],\"inception\\t\":[{\"hasValue\":{\"label\":\"1189\"}}],\"headquarters location\":[{\"hasValue\":{\"uri\":\"http://ontology.ontotext.com/resource/tslk4j12ot8g\",\"label\":\"City Hall\"}}],\"continent\":[{\"hasValue\":{\"uri\":\"http://ontology.ontotext.com/resource/tsk5kunnhtz4\",\"label\":\"Europe\"}}],\"country\":[{\"hasValue\":{\"uri\":\"http://ontology.ontotext.com/resource/tsk7r28yui9s\",\"label\":\"United Kingdom\"}},{\"hasValue\":{\"uri\":\"http://ontology.ontotext.com/resource/tsk4vb736328\",\"label\":\"England\"}}],\"population\":[{\"hasValue\":{\"label\":\"8416535\"},\"genericProperty\":{\"label\":\"8416535\"}},{\"hasValue\":{\"label\":\"1011157\"},\"genericProperty\":{\"label\":\"1011157\"}},{\"hasValue\":{\"label\":\"1197673\"},\"genericProperty\":{\"label\":\"1197673\"}},{\"hasValue\":{\"label\":\"1450122\"},\"genericProperty\":{\"label\":\"1450122\"}},{\"hasValue\":{\"label\":\"1729949\"},\"genericProperty\":{\"label\":\"1729949\"}},{\"hasValue\":{\"label\":\"1917013\"},\"genericProperty\":{\"label\":\"1917013\"}},{\"hasValue\":{\"label\":\"2286609\"},\"genericProperty\":{\"label\":\"2286609\"}},{\"hasValue\":{\"label\":\"3094391\"},\"genericProperty\":{\"label\":\"3094391\"}},{\"hasValue\":{\"label\":\"3902178\"},\"genericProperty\":{\"label\":\"3902178\"}},{\"hasValue\":{\"label\":\"4709960\"},\"genericProperty\":{\"label\":\"4709960\"}},{\"hasValue\":{\"label\":\"5565856\"},\"genericProperty\":{\"label\":\"5565856\"}},{\"hasValue\":{\"label\":\"6226494\"},\"genericProperty\":{\"label\":\"6226494\"}},{\"hasValue\":{\"label\":\"7157875\"},\"genericProperty\":{\"label\":\"7157875\"}},{\"hasValue\":{\"label\":\"7553526\"},\"genericProperty\":{\"label\":\"7553526\"}},{\"hasValue\":{\"label\":\"8098942\"},\"genericProperty\":{\"label\":\"8098942\"}},{\"hasValue\":{\"label\":\"7987936\"},\"genericProperty\":{\"label\":\"7987936\"}},{\"hasValue\":{\"label\":\"8164416\"},\"genericProperty\":{\"label\":\"8164416\"}},{\"hasValue\":{\"label\":\"7781342\"},\"genericProperty\":{\"label\":\"7781342\"}},{\"hasValue\":{\"label\":\"7449184\"},\"genericProperty\":{\"label\":\"7449184\"}},{\"hasValue\":{\"label\":\"6608513\"},\"genericProperty\":{\"label\":\"6608513\"}},{\"hasValue\":{\"label\":\"6887280\"},\"genericProperty\":{\"label\":\"6887280\"}},{\"hasValue\":{\"label\":\"7172036\"},\"genericProperty\":{\"label\":\"7172036\"}}]}";

    private static final String BAD_JSON = "{\"i am bad\": json}";
    OntoClientImpl ontoClient;

    @Before
    public void setUp() throws Exception {
        Client client = mock(Client.class);
        WebTarget webTarget = mock(WebTarget.class);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        response = mock(Response.class);

        when(builder.get()).thenReturn(response);
        when(builder.post(anyObject())).thenReturn(response);
        when(builder.header(anyString(), anyString())).thenReturn(builder);
        when(webTarget.request()).thenReturn(builder);
        when(webTarget.request(MediaType.TEXT_PLAIN)).thenReturn(builder);
        when(webTarget.request(MediaType.TEXT_XML)).thenReturn(builder);
        when(webTarget.path(anyString())).thenReturn(webTarget);
        when(webTarget.queryParam(anyString(), anyObject())).thenReturn(webTarget);
        when(client.target(anyString())).thenReturn(webTarget);

        ontoClient = new OntoClientImpl("", "");
        MemberModifier
                .field(OntoClientImpl.class, "client").set(
                ontoClient, client);
    }

    @Test
    public void success() throws IOException {
        InputStream extractResponseInputStream = new ByteArrayInputStream(EXTRACT_RESPONSE.getBytes(StandardCharsets.UTF_8));
        InputStream conceptResponseInputStream = new ByteArrayInputStream(CONCEPT_RESPONSE.getBytes(StandardCharsets.UTF_8));

        when(response.readEntity(InputStream.class)).thenReturn(extractResponseInputStream, conceptResponseInputStream);
        assertThat(ontoClient.extractCoordinatesFromTextByRelevance(anyString()), is(equalTo("51.507222222222°N 0.1275°W")));
    }

    @Test
    public void nullOutputForMissingLocation() throws IOException {
        InputStream extractResponseInputStream = new ByteArrayInputStream(EXTRACT_RESPONSE_WITHOUT_LOCATION.getBytes(StandardCharsets.UTF_8));

        when(response.readEntity(InputStream.class)).thenReturn(extractResponseInputStream);
        assertThat(ontoClient.extractCoordinatesFromTextByRelevance(anyString()), is(nullValue()));
    }

    @Test(expected = JsonParseException.class)
    public void exceptionExpectedOnMalformedJson() throws IOException {
        InputStream extractResponseInputStream = new ByteArrayInputStream(BAD_JSON.getBytes(StandardCharsets.UTF_8));

        when(response.readEntity(InputStream.class)).thenReturn(extractResponseInputStream);
        ontoClient.extractCoordinatesFromTextByRelevance(anyString());
        assertTrue(false); // should never reach
    }

    final String BASE_URL = "http://efd.ontotext.com/enrichment/extract";

    @Test
    public void extractSuccessMockTest() throws IOException {
        InputStream responseIS = getClass().getResourceAsStream("/extract-response.json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(responseIS, writer, Charset.defaultCharset());
        String body = writer.toString();

        when(response.readEntity(String.class)).thenReturn(body);
        String tags = ontoClient.extract("", "");
        System.out.println(tags); //TODO: finish the test
    }

    @Test
    @Ignore // TODO: after finishing, remove annotation
    public void extract2Object() throws JsonParseException, IOException {
        ExtractResponseDTO dto = ontoClient.extract2Object(null, null);
        System.out.println(dto); //TODO: finish the test
    }

}
