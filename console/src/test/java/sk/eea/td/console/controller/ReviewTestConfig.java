package sk.eea.td.console.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import sk.eea.td.config.PersistenceConfig;
import sk.eea.td.config.RESTClientsConfig;
import sk.eea.td.config.SecurityConfig;
import sk.eea.td.config.WebConfig;
import sk.eea.td.service.ApprovementService;

@Configuration
@PropertySource({"classpath:default.properties", "classpath:integration.properties"})
@ComponentScan(basePackages = {"sk.eea.td.console", "sk.eea.td.service", "sk.eea.td.mapper", "sk.eea.td.rest"})
@Import({WebConfig.class, SecurityConfig.class, PersistenceConfig.class, RESTClientsConfig.class})
public class ReviewTestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
