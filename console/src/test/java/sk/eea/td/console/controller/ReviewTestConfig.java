package sk.eea.td.console.controller;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import sk.eea.td.config.PersistenceConfig;
import sk.eea.td.config.SecurityConfig;
import sk.eea.td.config.WebConfig;

@Configuration
@PropertySource({"classpath:default.properties", "classpath:integration.properties"})
@ComponentScan(basePackages = {"sk.eea.td.console", "sk.eea.td.flow", "sk.eea.td.mapper", "sk.eea.td.service"})
@Import({WebConfig.class, SecurityConfig.class, PersistenceConfig.class})
public class ReviewTestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
