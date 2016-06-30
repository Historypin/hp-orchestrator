package sk.eea.td.console.controller;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import sk.eea.td.config.PersistenceConfig;
import sk.eea.td.config.SecurityConfig;
import sk.eea.td.config.WebConfig;

@Configuration
@PropertySource({"classpath:default.properties", "classpath:${spring.profiles.active:prod}.properties"})
@ComponentScan(basePackages = "sk.eea.td.console")
@Import({WebConfig.class, SecurityConfig.class, PersistenceConfig.class})
public class ReviewTestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
