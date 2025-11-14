package api.model.churn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "branchName",
        "timestamp"
})
@Generated("jsonschema2pojo")
public class Commit {

    @JsonProperty("type")
    private String type;
    @JsonProperty("branchName")
    private String branchName;
    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("branchName")
    public String getBranchName() {
        return branchName;
    }

    @JsonProperty("timestamp")
    public Long getTimestamp() {
        return timestamp;
    }

}
