package sk.eea.td.rest.service;

import java.nio.file.Path;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sk.eea.td.flow.FlowException;
import sk.eea.td.mint_client.api.MintClient;
import sk.eea.td.mint_client.api.MintServiceException;
import sk.eea.td.mint_client.impl.MintClientImpl;

@Component
public class MintStoreService {

	private static final Logger LOG = LoggerFactory.getLogger(MintStoreService.class);
	
	@Value("${mint.username}")
	private String mintUser;
	
	@Value("${mint.baseUrl}")
	private String mintBase;
	
	@Value("${mint.userpass}")
	private String mintPass;

	public boolean store(Path file) throws FlowException {
		LOG.debug(MessageFormat.format("Storing MINT data ({0}}", file.getFileName()));
		MintClient client = MintClientImpl.getNewClient(mintBase);
		try {
			if(!client.login(mintUser, mintPass))
				return false;
			int id = client.uploadJson(file.toFile());
			if(Integer.valueOf(0).equals(id))
				return false;
			if(!client.defineItems(id))
				return false;
			if(!client.transform(id))
				return false;
			if(!client.publish(id))
				return false;
		}catch (MintServiceException e){
			throw new FlowException("Save to MINT error", e);
		}
		LOG.debug(MessageFormat.format("Storing MINT data ({0}) SUCCESS", file.getFileName()));
		return true;
	}
		
}
