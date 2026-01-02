package io.teamscale.api.model.commit.chart;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "startTimestamp",
        "endTimestamp",
        "commits"
})
@Generated("jsonschema2pojo")
public class CommitChart {

    @JsonProperty("startTimestamp")
    private Long startTimestamp;
    @JsonProperty("endTimestamp")
    private Long endTimestamp;
    @JsonProperty("commits")
    private List<UserWithCommits> userWithCommits;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("startTimestamp")
    public Long getStartTimestamp() {
        return startTimestamp;
    }

    @JsonProperty("startTimestamp")
    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @JsonProperty("endTimestamp")
    public Long getEndTimestamp() {
        return endTimestamp;
    }

    @JsonProperty("endTimestamp")
    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    @JsonProperty("commits")
    public List<UserWithCommits> getCommits() {
        return userWithCommits;
    }

    @JsonProperty("commits")
    public void setCommits(List<UserWithCommits> userWithCommits) {
        this.userWithCommits = userWithCommits;
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
