package sk.eea.td.console.model;

/**
 * Enum contains all possible names for keys used in job parameters.
 *
 */
public enum ParamKey {

    HP_USER_ID("historypinUserId"),
    HP_API_KEY("historypinApiKey"),
    HP_DATE("collectionDate"),
    HP_TAGS("collectionTags"),
    HP_NAME("collectionName"),
    HP_LAT("collectionLat"),
    HP_LNG("collectionLng"),
    HP_RADIUS("collectionRadius");

    private String key;

    ParamKey(String key) {
        this.key = key;
    }
}
