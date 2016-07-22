package sk.eea.td.console.model;

/**
 * Enum contains all possible names for keys used in job parameters.
 *
 */
public enum ParamKey {

    HP_USER_ID,
    HP_API_KEY,
    HP_API_SECRET,
    HP_DATE,
    HP_TAGS,
    HP_NAME,
    HP_LAT,
    HP_LNG,
    HP_RADIUS,
    HP_PROJECT_SLUG,
    OAI_FROM,
    OAI_UNTIL,
    OAI_SET,
    EU_REST_QUERY,
    EU_REST_FACET,
    HARVEST_PATH,
    TRANSFORM_PATH,
    HP_UNTIL_CURRENT, 
    LAST_SUCCESS, 
    TAGAPP_BATCH, 
    /** Used from approval page, instructs flow to cleanup data and finish. */
    FINISH_FLOW, 
    /** Folder where approved data are stored */
    APPROVED_PATH,
    EMAIL_ATTACHMENT
}
