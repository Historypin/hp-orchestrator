package sk.eea.td.tagapp_client;

import java.util.Map;

public class CulturalObjectDTO {

	private Long id;
	private String author;	
	private String externalId;
	private String externalUrl;
	private String externalSource;
	private String batchId;
	private Map<String, String> description;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getExternalId() {
		return externalId;
	}
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	public String getExternalUrl() {
		return externalUrl;
	}
	public void setExternalUrl(String externalUrl) {
		this.externalUrl = externalUrl;
	}
	public String getExternalSource() {
		return externalSource;
	}
	public void setExternalSource(String externalSource) {
		this.externalSource = externalSource;
	}
	public String getBatchId() {
		return batchId;
	}
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	public Map<String, String> getDescription() {
		return this.description;
	}
	public void setDescription(Map<String, String> description) {
		this.description = description;
	}
}
