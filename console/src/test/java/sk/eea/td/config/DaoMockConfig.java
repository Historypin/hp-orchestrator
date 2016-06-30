package sk.eea.td.config;

import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.init.ScriptParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring4.SpringTemplateEngine;

import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.console.repository.ParamRepository;

@Configuration
public class DaoMockConfig {
	
	@Bean
	public JobRepository getJobRepository(){
		return EasyMock.createMock(JobRepository.class);
	}

	@Bean
	public JobRunRepository getJobRunRepository(){
		return EasyMock.createMock(JobRunRepository.class);
	}

	@Bean
	public LogRepository getLogRepository(){
		return EasyMock.createMock(LogRepository.class);
	}
	
	@Bean
	public ParamRepository getParamRepository(){
		return EasyMock.createMock(ParamRepository.class);
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