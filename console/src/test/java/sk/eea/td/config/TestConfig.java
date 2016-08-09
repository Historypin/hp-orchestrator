package sk.eea.td.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.springframework.security.core.token.SecureRandomFactoryBean;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
@ComponentScan(basePackages = {"sk.eea.td.flow", "sk.eea.td.rest", "sk.eea.td.mapper", "sk.eea.td.service"})
@PropertySource({ "classpath:default.properties", "classpath:integration.properties"})
public class TestConfig {

    @Value("${templateResolver.cacheable}")
    Boolean cacheable;
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }    

    @Bean
    public ClassLoaderTemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setPrefix("mail/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(cacheable);

        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(emailTemplateResolver());
        return engine;
    }

    @Bean
    public JavaMailSender mailSender(Environment environment) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(environment.getProperty("mail.server.host"));
        mailSender.setPort(environment.getProperty("mail.server.port", Integer.class));
        mailSender.setProtocol(environment.getProperty("mail.server.protocol"));
        Properties properties = new Properties();
        properties.put("mail.smtp.localhost", environment.getProperty("mail.smtp.localhost"));
        mailSender.setJavaMailProperties(properties);
        return mailSender;
    }

    @Bean
    public SecureRandomFactoryBean secureRandomFactoryBean() {
        return new SecureRandomFactoryBean();
    }

    @Bean
    public KeyBasedPersistenceTokenService keyBasedPersistenceTokenService(@Value("${token.server.secret}") String serverSecret, @Value("${token.server.integer}") Integer serverInteger) throws Exception {
        KeyBasedPersistenceTokenService tokenService = new KeyBasedPersistenceTokenService();
        tokenService.setSecureRandom(secureRandomFactoryBean().getObject());
        tokenService.setServerSecret(serverSecret);
        tokenService.setServerInteger(serverInteger);
        return tokenService;
    }
}

