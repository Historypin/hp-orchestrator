package sk.eea.td.hp_client.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {

    final ObjectMapper defaultObjectMapper;

    public JacksonObjectMapperProvider() {
        defaultObjectMapper = createDefaultObjectMapper();
    }

    @Override
    public ObjectMapper getContext(final Class<?> type) {
            return defaultObjectMapper;
    }

    private static ObjectMapper createDefaultObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    }
}
