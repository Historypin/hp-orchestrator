package sk.eea.td.hp_client.util;

import java.util.Map;
import java.util.TreeMap;

public class ApiTokenUtility {

    public static final String API_KEY = "d82e14f622f1fbd475da1ff2fba8b118f4756f368fd7f7a5a30f8beb705d743a";

    public static final String API_SECRET = "c61b9a9ad72b02264ac1997165309316";

    public static void main(String[] args) {
        Map<String, String> data = new TreeMap<>();

        data.put("api_key", API_KEY);
        data.put("caption", "My test pin");
        data.put("timemap[lat]", "42");
        data.put("timemap[lng]", "23");
        data.put("repinned_projects[0][id]", "25464");
        data.put("date", "2016-02-10");
        data.put("license", "no-copyright");
        data.put("pinner_type", "text");
        data.put("display[content]", "This is my pin content");
        data.put("api_path", "pin/save.json");

        ApiTokenFactory factory = new ApiTokenFactory(API_SECRET);
        String apiToken = factory.getApiToken(data);
        System.out.println("ApiToken: " + apiToken);
    }
}
