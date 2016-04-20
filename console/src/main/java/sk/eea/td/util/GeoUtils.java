package sk.eea.td.util;

public class GeoUtils {

    public static Long EARTH_RADIUS = 6371000L;

    /**
     * Calculates distance in meters between two places on Earth, described by their position [latitude, longitude].
     * For calculation method uses 'haversine' formula as described <a href="http://www.movable-type.co.uk/scripts/latlong.html#ortho-dist" >here</a>.
     *
     * @param first  first location.
     * @param second second location.
     * @return distance between locations in meters.
     */
    public static Long calculateDistance(Location first, Location second) {
        final Double phi1 = Math.toRadians(first.getLat());
        final Double phi2 = Math.toRadians(second.getLat());
        final Double deltaPhi = Math.toRadians(second.getLat() - first.getLat());
        final Double deltaLambda = Math.toRadians(second.getLng() - first.getLng());

        final Double a =
                Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
                        + Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        final Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (long) (EARTH_RADIUS * c);
    }

    /**
     * Calculates middle point between two locations, described by their position [latitude, longitude].
     * Uses formula described <a href="http://www.movable-type.co.uk/scripts/latlong.html#midpoint" >here</a>.
     *
     * @param first  first location.
     * @param second second location.
     * @return location of midpoint.
     */
    public static Location calculateMidpoint(Location first, Location second) {
        final Double phi1 = Math.toRadians(first.getLat());
        final Double phi2 = Math.toRadians(second.getLat());
        final Double deltaLambda = Math.toRadians(second.getLng() - first.getLng());

        final Double Bx = Math.cos(phi2) * Math.cos(deltaLambda);
        final Double By = Math.cos(phi2) * Math.sin(deltaLambda);
        final Double phi3 = Math.atan2(
                Math.sin(phi1) + Math.sin(phi2),
                Math.sqrt(Math.pow(Math.cos(phi1) + Bx, 2) + Math.pow(By, 2))
        );
        final Double lambda3 = Math.toRadians(first.getLng()) + Math.atan2(By, Math.cos(phi1) + Bx);
        return new Location(Math.toDegrees(phi3), normalizeLongitudeCoordinate(Math.toDegrees(lambda3)));
    }

    /**
     * Normalizes longitude coordinates between (-180, +180) degrees.
     * E.g.:
     * <pre><code>Double d = normalizeLongitudeCoordinate(-181);</code></pre>
     * Variable 'd' wil have value 179.0.
     *
     * @param d coordinates in degrees.
     * @return normalized coordinates in degrees.
     */
    public static Double normalizeLongitudeCoordinate(Double d) {
        return (d + 540) % 360 - 180;
    }

    /**
     * Wrapper class which symbolized location on a sphere using latitude and longitude.
     */
    public static class Location {

        private Double lat;

        private Double lng;

        public Location() {
        }

        public Location(Double lat, Double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Location location = (Location) o;

            if (getLat() != null ? !getLat().equals(location.getLat()) : location.getLat() != null)
                return false;
            return getLng() != null ? getLng().equals(location.getLng()) : location.getLng() == null;

        }

        @Override
        public int hashCode() {
            int result = getLat() != null ? getLat().hashCode() : 0;
            result = 31 * result + (getLng() != null ? getLng().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Location{" +
                    "lat=" + lat +
                    ", lng=" + lng +
                    '}';
        }
    }
}
