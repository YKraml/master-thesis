package api.teamscale.model.cleaned;

import api.teamscale.model.churn.FindingChurn;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "findingChurn",
        "metrics"
})
@Generated("jsonschema2pojo")
public class TeamscaleDataEntry {

    @JsonProperty("findingChurn")
    private FindingChurn findingChurn;
    @JsonProperty("metrics")
    private CleanedMetrics cleanedMetrics;

    public static TeamscaleDataEntry of(FindingChurn findingChurn, CleanedMetrics cleanedMetrics) {
        TeamscaleDataEntry teamscaleDataEntry = new TeamscaleDataEntry();
        teamscaleDataEntry.findingChurn = findingChurn;
        teamscaleDataEntry.cleanedMetrics = cleanedMetrics;
        return teamscaleDataEntry;
    }

    @JsonProperty("findingChurn")
    public FindingChurn getFindingChurn() {
        return findingChurn;
    }

    @JsonProperty("metrics")
    public CleanedMetrics getMetrics() {
        return cleanedMetrics;
    }
}
