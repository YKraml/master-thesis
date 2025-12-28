package api.teamscale.model.commit.chart;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "fullName",
        "commits"
})
@Generated("jsonschema2pojo")
public class UserWithCommits {

    @JsonProperty("fullName")
    private String fullName;
    @JsonProperty("commits")
    private List<Commits> commits;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("fullName")
    public String getFullName() {
        return fullName;
    }

    @JsonProperty("fullName")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @JsonProperty("commits")
    public List<Commits> getCommits() {
        return commits;
    }

    @JsonProperty("commits")
    public void setCommits(List<Commits> commits) {
        this.commits = commits;
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
