package sk.eea.td.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static sk.eea.td.util.GeoUtils.*;

public class GeoUtilsTest {

    @Test
    public void testNormalizeCoordinate() throws Exception {
        assertThat(normalizeLongitudeCoordinate(-240d), is(equalTo(120d)));
        assertThat(normalizeLongitudeCoordinate(0d), is(equalTo(0d)));
        assertThat(normalizeLongitudeCoordinate(-181d), is(equalTo(179d)));
    }

    @Test
    public void testCalculateDistance() throws Exception {
        assertThat(calculateDistance(new GeoUtils.Location(0d, 0d),  new GeoUtils.Location(0d, 0d)), is(equalTo(0L)));
        assertThat(calculateDistance(new GeoUtils.Location(40.7486, -73.9864), new GeoUtils.Location(43.1248, 21.4942)), is(equalTo(7431595L)));
        assertThat(calculateDistance(new GeoUtils.Location(10.46, 43.124), new GeoUtils.Location(-52.0184, -74.0124)), is(equalTo(12762903L)));
        assertThat(calculateDistance(new GeoUtils.Location(48.160756, 17.092353), new GeoUtils.Location(48.162896, 17.094370)), is(equalTo(281L)));
    }

    @Test
    public void testCalculateMidpoint() throws Exception {
        assertThat(calculateMidpoint(new GeoUtils.Location(0d, 0d),  new GeoUtils.Location(0d, 0d)), is(equalTo(new GeoUtils.Location(0d , 0d))));

        GeoUtils.Location location = calculateMidpoint(new GeoUtils.Location(40.7486, -73.9864), new GeoUtils.Location(43.1248, 21.4942));
        assertThat(location.getLat(), is(closeTo(53.177914723230266, 0.0005)));
        assertThat(location.getLng(), is(closeTo(-27.42081507366845, 0.0005)));

        location = calculateMidpoint(new GeoUtils.Location(10.46, 43.124), new GeoUtils.Location(-52.0184, -74.0124));
        assertThat(location.getLat(), is(closeTo(-34.25285837888691, 0.0005)));
        assertThat(location.getLng(), is(closeTo(5.19158656307593, 0.0005)));

        location = calculateMidpoint(new GeoUtils.Location(48.160756, 17.092353), new GeoUtils.Location(48.162896, 17.094370));
        assertThat(location.getLat(), is(closeTo(48.16182600441081, 0.0005)));
        assertThat(location.getLng(), is(closeTo(17.093361478963857, 0.0005)));
    }
}
