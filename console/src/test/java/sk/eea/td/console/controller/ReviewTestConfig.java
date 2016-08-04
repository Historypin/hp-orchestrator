package sk.eea.td.console.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.config.PersistenceConfig;
import sk.eea.td.config.RESTClientsConfig;
import sk.eea.td.config.SecurityConfig;
import sk.eea.td.config.WebConfig;

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
