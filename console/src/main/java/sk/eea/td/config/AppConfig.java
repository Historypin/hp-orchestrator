package sk.eea.td.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "sk.eea.td")
public class AppConfig {

    @Bean
    public ViewResolver viewResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");

        if("dev".equalsIgnoreCase(System.getProperty("spring.profiles.active"))) {
            templateResolver.setCacheable(false);
        }

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver);

        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(engine);
        viewResolver.setOrder(1);
        return viewResolver;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer =  new PropertySourcesPlaceholderConfigurer();
        // get active profile
        String activeProfile = System.getProperty("spring.profiles.active");

        // choose different property files for different active profile
        Resource profileResource;
        if ("dev".equalsIgnoreCase(activeProfile)) {
            profileResource = new ClassPathResource("dev.properties");
        } else if ("test".equalsIgnoreCase(activeProfile)) {
            profileResource = new ClassPathResource("test.properties");
        } else {
            profileResource = new ClassPathResource("prod.properties");
        }

        Resource defaultResource = new ClassPathResource("default.properties");
        // load the property files
        propertySourcesPlaceholderConfigurer.setLocations(defaultResource, profileResource);

        return propertySourcesPlaceholderConfigurer;
    }
}
