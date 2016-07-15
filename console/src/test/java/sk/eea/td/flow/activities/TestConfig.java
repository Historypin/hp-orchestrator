package sk.eea.td.flow.activities;

import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.rest.service.CleanupService;
import sk.eea.td.rest.service.EuropeanaStoreService;
import sk.eea.td.rest.service.HistorypinStoreService;
import sk.eea.td.rest.service.MailService;
import sk.eea.td.rest.service.MintStoreService;

@Configuration
@PropertySource({ "classpath:default.properties", "classpath:integration.properties"})
@ComponentScan(basePackages = {"sk.eea.td.flow.activities"})
public class TestConfig {
	
	public TestConfig() {
	}
	
	@Bean
	public MintStoreService getMintStoreService(){
		return EasyMock.createStrictMock(MintStoreService.class);
	}
	
	@Bean 
	public HistorypinStoreService getHistorypinStoreService(){
		return EasyMock.createStrictMock(HistorypinStoreService.class);
	}
	
	@Bean
	public StoreActivity getStoreActivity(){
		return new StoreActivity();
	}
	
    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
    
    @Bean
    public Tagapp2HPTransformActivity getTagapp2HPTransformActivity(){
        return new Tagapp2HPTransformActivity();
    }
    
    @Bean
    public LogRepository getLogRepository(){
    	return EasyMock.createStrictMock(LogRepository.class);
    }
    
    @Bean
    public EuropeanaStoreService getEuropeanaStoreService(){
    	return new EuropeanaStoreService();
    }
    
    @Bean 
    public HP_A2EU_ATransformActivity geHp_A2EU_ATransformActivity(){
    	return new HP_A2EU_ATransformActivity();
    }
    
    @Bean
    public CleanupService getCleanupService(){
    	return EasyMock.createMock(CleanupService.class);
    }
    
    @Bean
    public KeyBasedPersistenceTokenService getTokenService(){
        return EasyMock.createMock(KeyBasedPersistenceTokenService.class);
    }
    
    @Bean
    public MailService getMailService(){
        return EasyMock.createMock(MailService.class);
    }
    @Bean
    public SpringTemplateEngine getSpringTemplateEngine(){
        return EasyMock.createMock(SpringTemplateEngine.class);
    }
    
    @Bean
    public JavaMailSender getJavaMailSender(){
        return EasyMock.createMock(JavaMailSender.class);
    }
    

}