package teamscale.api.model.churn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.processing.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "findingSummary",
        "addedFindings",
        "findingsInChangedCode",
        "removedFindings"
})
@Generated("jsonschema2pojo")
public class Churn {

    @JsonProperty("findingSummary")
    private FindingSummary findingSummary;
    @JsonProperty("addedFindings")
    private Findings addedFindings;
    @JsonProperty("findingsInChangedCode")
    private Findings findingsInChangedCode;
    @JsonProperty("removedFindings")
    private Findings removedFindings;

    @JsonProperty("findingSummary")
    public FindingSummary getFindingSummary() {
        return findingSummary;
    }

    @JsonProperty("addedFindings")
    public Findings getAddedFindings() {
        return addedFindings;
    }

    @JsonProperty("findingsInChangedCode")
    public Findings getFindingsInChangedCode() {
        return findingsInChangedCode;
    }

    @JsonProperty("removedFindings")
    public Findings getRemovedFindings() {
        return removedFindings;
    }

}
