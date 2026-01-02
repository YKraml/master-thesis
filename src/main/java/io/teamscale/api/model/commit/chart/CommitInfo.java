package io.teamscale.api.model.commit.chart;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "branchName",
        "timestamp",
        "parentCommits"
})
@Generated("jsonschema2pojo")
public class CommitInfo {

    @JsonProperty("type")
    private String type;
    @JsonProperty("branchName")
    private String branchName;
    @JsonProperty("timestamp")
    private Long timestamp;
    @JsonProperty("parentCommits")
    private List<ParentCommit> parentCommits;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("branchName")
    public String getBranchName() {
        return branchName;
    }

    @JsonProperty("branchName")
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    @JsonProperty("timestamp")
    public Long getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("parentCommits")
    public List<ParentCommit> getParentCommits() {
        return parentCommits;
    }

    @JsonProperty("parentCommits")
    public void setParentCommits(List<ParentCommit> parentCommits) {
        this.parentCommits = parentCommits;
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
