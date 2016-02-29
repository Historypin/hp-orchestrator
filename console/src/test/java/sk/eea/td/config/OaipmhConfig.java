package sk.eea.td.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sk.eea.td.rest.service.OaipmhHarvestService;


@Configuration
public class OaipmhConfig {

    @Bean
    public OaipmhHarvestService oaipmhHarvestService() {
        return new OaipmhHarvestService();
    }
}
