package sk.eea.td.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowManager;
import sk.eea.td.flow.FlowManagerImpl;
import sk.eea.td.flow.activities.HarvestActivity;
import sk.eea.td.flow.activities.TransformAndStoreActivity;
import sk.eea.td.rest.model.Connector;

import java.util.Locale;

@Configuration
@EnableWebMvc
@EnableScheduling
@ComponentScan(basePackages = "sk.eea.td")
public class AppConfig extends WebMvcConfigurerAdapter {

    @Bean
    public ClassLoaderTemplateResolver templateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");

        if ("dev".equalsIgnoreCase(System.getProperty("spring.profiles.active"))) {
            templateResolver.setCacheable(false);
        }

        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        return engine;
    }

    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setOrder(1);
        return viewResolver;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
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

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
        sessionLocaleResolver.setDefaultLocale(Locale.ENGLISH);
        return sessionLocaleResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // static content
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
        registry.addResourceHandler("/img/**").addResourceLocations("/img/");
        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
        // swagger html
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        // webjars
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public Activity harvestActivity() {
        return new HarvestActivity();
    }

    @Bean
    public Activity transformAndStoreActivity() {
        return new TransformAndStoreActivity();
    }

    @Bean
    public FlowManager europeanaFlowManager() {
        FlowManagerImpl flowManager = new FlowManagerImpl(Connector.EUROPEANA, Connector.OAIPMH);
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(transformAndStoreActivity());
        return flowManager;
    }

    @Schedules(
            @Scheduled(cron = "${europeana.flm.cron.expression}")
    )
    public void europeanaFlowManagerTimeSignal() {
        europeanaFlowManager().trigger();
    }

//    @Bean
//    public FlowManager historypinHarvester(){
//    	FlowManagerImpl flowManager = new FlowManagerImpl("HP");
//        flowManager.addActivity(new HarvestActivity());
//        flowManager.addActivity(new TransformActivity());
//        flowManager.addActivity(new StoreActivity());
//        return flowManager;
//    }
//
//    @Schedules(@Scheduled(cron="${historypin.flm.cron.expression}"))
//    public void historypinFlowManagerTimeSignal(){
//        historypinHarvester().trigger();
//    }
}
