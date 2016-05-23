package sk.eea.td.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring4.SpringTemplateEngine;

@Configuration
@ComponentScan(basePackages = {"sk.eea.td.flow", "sk.eea.td.rest", "sk.eea.td.mapper"})
@PropertySource({ "classpath:default.properties", "classpath:${spring.profiles.active:dev}.properties"})
public class TestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }    

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        return engine;
    }

    @Bean
    public JavaMailSender mailSender(Environment environment) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        return mailSender;
    }
}

