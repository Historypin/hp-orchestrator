package sk.eea.td.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.impl.HPClientImpl;

@Configuration
public class RESTClientsConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public HPClient hpClient(
            @Value("${historypin.base.url}") String baseURL,
            @Value("${historypin.api.key}") String apiKey,
            @Value("${historypin.api.secret}") String apiSecret
    ) {
        return new HPClientImpl(baseURL, apiKey, apiSecret);
    }
}
