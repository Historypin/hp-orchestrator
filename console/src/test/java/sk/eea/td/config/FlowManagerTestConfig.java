package sk.eea.td.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
//@ComponentScan(basePackages = {"sk.eea.td.flow", "sk.eea.td.console.repository", "sk.eea.td.rest.service"})
@ComponentScan(basePackages = {"sk.eea.td"})
@PropertySource({ "classpath:default.properties", "classpath:integration.properties"})
public class FlowManagerTestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
