package sk.eea.td.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.eu_client.api.EuropeanaClient;
import sk.eea.td.eu_client.impl.EuropeanaClientImpl;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.impl.HPClientImpl;
import sk.eea.td.tagapp_client.TagappClient;
import sk.eea.td.tagapp_client.TagappClientImpl;

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

    @Bean
    public EuropeanaClient europeanaClient(
            @Value("${europeana.base.url}") String baseURL,
            @Value("${europeana.ws.key}") String wsKey) {
        return new EuropeanaClientImpl(baseURL, wsKey);
    }
    
    @Bean
    public TagappClient tagappClient(
            @Value("${tagapp.base.url}") String baseURL,
            @Value("${tagapp.username}") String username,
            @Value("${tagapp.password}") String password){
        return new TagappClientImpl(baseURL, username, password);
    }
}
