package sk.eea.td.tagapp_client;

public class TagDTO {
	private Long id;
	private String language;
	private String value;
	private Long culturalObjectId;
	
	private String culturalObjectExternalId;
    private String culturalObjectDescription;
    private String culturalObjectExternalUrl;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Long getCulturalObjectId() {
		return culturalObjectId;
	}
	public void setCulturalObjectId(Long culturalObjectId) {
		this.culturalObjectId = culturalObjectId;
	}
    public String getCulturalObjectExternalId() {
        return culturalObjectExternalId;
    }
    public void setCulturalObjectExternalId(String culturalObjectExternalId) {
        this.culturalObjectExternalId = culturalObjectExternalId;
    }
    public String getCulturalObjectDescription() {
        return culturalObjectDescription;
    }
    public void setCulturalObjectDescription(String culturalObjectDescription) {
        this.culturalObjectDescription = culturalObjectDescription;
    }
    public String getCulturalObjectExternalUrl() {
        return culturalObjectExternalUrl;
    }
    public void setCulturalObjectExternalUrl(String culturalObjectExternalUrl) {
        this.culturalObjectExternalUrl = culturalObjectExternalUrl;
    }
}
