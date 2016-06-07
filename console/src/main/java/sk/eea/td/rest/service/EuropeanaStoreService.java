package sk.eea.td.rest.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sk.eea.td.eu_client.api.EuropeanaAnnotationClient;
import sk.eea.td.eu_client.impl.EuropeanaAnnotationClientImpl;

@Component
public class EuropeanaStoreService {

	Logger LOG = LoggerFactory.getLogger(EuropeanaStoreService.class);
	
	private EuropeanaAnnotationClient europeanaAnnotationClient;

	@Value("${eu.annotation.url}")
	private String euAnnotationUrl;

	@Value("${eu.annotation.apikey}")
	private String euAnnotationAPIKey;

	@Value("${eu.annotation.user}")
	private String euAnnotationUser;
	
	@PostConstruct
	public void init() {
		this.europeanaAnnotationClient = new EuropeanaAnnotationClientImpl(euAnnotationUrl, euAnnotationAPIKey, euAnnotationUser);
	}
	
	public boolean storeAnnotation(Long projectId, Path file){
		
		try {
			String annotationJson = new String(Files.readAllBytes(file));
			europeanaAnnotationClient.createAnnotation(annotationJson);
		} catch (IOException e) {
			LOG.error(MessageFormat.format("Cannot create EU annotation. File ''{0}'' does not exist", file));
			return false;
		} catch (InterruptedException e) {
			LOG.error("Cannot create EU annotation. Thread was interrupted." );
			return false;
		}
		return true;
	}
}
