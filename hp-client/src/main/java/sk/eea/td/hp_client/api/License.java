package sk.eea.td.hp_client.api;

public enum License {
    COPYRIGHT("copyright"),
    NO_COPYRIGHT("no-copyright"),
    PUBLIC_DOMAIN("public-domain"),
    OPEN_GOVERNMENT("open-government"),
    CC_ZERO_1_0("http://creativecommons.org/publicdomain/zero/1.0/"),
    CC_BY_3_0("http://creativecommons.org/licenses/by/3.0/"),
    CC_BY_SA_3_0("http://creativecommons.org/licenses/by-sa/3.0/"),
    CC_BY_ND_2_0("http://creativecommons.org/licenses/by-nd/2.0/"),
    CC_BY_NC_3_0("http://creativecommons.org/licenses/by-nc/3.0/"),
    CC_BY_NC_SA_3_0("http://creativecommons.org/licenses/by-nc-sa/3.0/"),
    CC_BY_NC_ND_3_0("http://creativecommons.org/licenses/by-nc-nd/3.0/");

    private final String key;

    License(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
