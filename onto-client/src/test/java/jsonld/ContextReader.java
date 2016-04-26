package jsonld;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

public class ContextReader {

    @Test
    public void test() throws IOException, JsonLdError {
        InputStream jsonIS = getClass().getResourceAsStream("/extract-response.json");
        Object json = JsonUtils.fromInputStream(jsonIS);

        InputStream contextIS = getClass().getResourceAsStream("/efd-context.jsonld");
        Object context = JsonUtils.fromInputStream(contextIS);

        JsonLdOptions options = new JsonLdOptions();

        Object compact = JsonLdProcessor.compact(json, context, options);
        System.out.println(JsonUtils.toPrettyString(compact));

        Object flat = JsonLdProcessor.flatten(json, context, options);
        System.out.println(JsonUtils.toPrettyString(flat));

        Object normal = JsonLdProcessor.normalize(json, options);
        System.out.println(JsonUtils.toPrettyString(normal));

        Object expand = JsonLdProcessor.expand(json, options);
        System.out.println(JsonUtils.toPrettyString(expand));
    }
}
