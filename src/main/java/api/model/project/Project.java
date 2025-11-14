package api.model.project;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "internalId",
        "externalStorageProjectMappingId",
        "name",
        "publicIds",
        "creationTimestamp",
        "deleting",
        "reanalyzing",
        "copyingData",
        "configurationCompleted"
})
@Generated("jsonschema2pojo")
public class Project {

    @JsonProperty("internalId")
    private String internalId;
    @JsonProperty("externalStorageProjectMappingId")
    private String externalStorageProjectMappingId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("publicIds")
    private List<String> publicIds;
    @JsonProperty("creationTimestamp")
    private Long creationTimestamp;
    @JsonProperty("deleting")
    private Boolean deleting;
    @JsonProperty("reanalyzing")
    private Boolean reanalyzing;
    @JsonProperty("copyingData")
    private Boolean copyingData;
    @JsonProperty("configurationCompleted")
    private Boolean configurationCompleted;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("internalId")
    public String getInternalId() {
        return internalId;
    }

    @JsonProperty("internalId")
    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    @JsonProperty("externalStorageProjectMappingId")
    public String getExternalStorageProjectMappingId() {
        return externalStorageProjectMappingId;
    }

    @JsonProperty("externalStorageProjectMappingId")
    public void setExternalStorageProjectMappingId(String externalStorageProjectMappingId) {
        this.externalStorageProjectMappingId = externalStorageProjectMappingId;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("publicIds")
    public List<String> getPublicIds() {
        return publicIds;
    }

    @JsonProperty("publicIds")
    public void setPublicIds(List<String> publicIds) {
        this.publicIds = publicIds;
    }

    @JsonProperty("creationTimestamp")
    public Long getCreationTimestamp() {
        return creationTimestamp;
    }

    @JsonProperty("creationTimestamp")
    public void setCreationTimestamp(Long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    @JsonProperty("deleting")
    public Boolean getDeleting() {
        return deleting;
    }

    @JsonProperty("deleting")
    public void setDeleting(Boolean deleting) {
        this.deleting = deleting;
    }

    @JsonProperty("reanalyzing")
    public Boolean getReanalyzing() {
        return reanalyzing;
    }

    @JsonProperty("reanalyzing")
    public void setReanalyzing(Boolean reanalyzing) {
        this.reanalyzing = reanalyzing;
    }

    @JsonProperty("copyingData")
    public Boolean getCopyingData() {
        return copyingData;
    }

    @JsonProperty("copyingData")
    public void setCopyingData(Boolean copyingData) {
        this.copyingData = copyingData;
    }

    @JsonProperty("configurationCompleted")
    public Boolean getConfigurationCompleted() {
        return configurationCompleted;
    }

    @JsonProperty("configurationCompleted")
    public void setConfigurationCompleted(Boolean configurationCompleted) {
        this.configurationCompleted = configurationCompleted;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
