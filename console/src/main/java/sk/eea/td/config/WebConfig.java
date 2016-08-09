package sk.eea.td.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @PostConstruct
    public void init() {
        ClassLoaderTemplateResolver emailResolver = new ClassLoaderTemplateResolver();
        emailResolver.setTemplateMode("HTML5");
        emailResolver.setPrefix("mail/");
        emailResolver.setSuffix(".html");
        emailResolver.setOrder(templateEngine.getTemplateResolvers().size());
        emailResolver.setCacheable(false);
        templateEngine.addTemplateResolver(emailResolver);
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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // swagger html
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
    }
}
