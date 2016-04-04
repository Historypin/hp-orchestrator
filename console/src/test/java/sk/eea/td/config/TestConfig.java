package sk.eea.td.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = {"sk.eea.td.flow", "sk.eea.td.rest"})
public class TestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return AppConfig.propertyConfigurer();
    }
}
