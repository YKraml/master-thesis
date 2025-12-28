package api.teamscale.model.cleaned;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "repoOwner",
        "repoName",
        "entries"
})
@Generated("jsonschema2pojo")
public class TeamscaleData {

    @JsonProperty("repoOwner")
    private String repoOwner;
    @JsonProperty("repoName")
    private String repoName;
    @JsonProperty("entries")
    private List<TeamscaleDataEntry> entries;

    public static TeamscaleData of(String repoOwner, String repoName, List<TeamscaleDataEntry> entries) {
        TeamscaleData teamscaleData = new TeamscaleData();
        teamscaleData.repoOwner = repoOwner;
        teamscaleData.repoName = repoName;
        teamscaleData.entries = entries;
        return teamscaleData;
    }

    @JsonProperty("repoOwner")
    public String getRepoOwner() {
        return repoOwner;
    }

    @JsonProperty("repoName")
    public String getRepoName() {
        return repoName;
    }

    @JsonProperty("entries")
    public List<TeamscaleDataEntry> getEntries() {
        return entries;
    }
}
