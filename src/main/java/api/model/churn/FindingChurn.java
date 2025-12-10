package api.model.churn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "commit",
        "addedFindings",
        "findingsAddedInBranch",
        "findingsInChangedCode",
        "removedFindings",
        "findingsRemovedInBranch",
})
@Generated("jsonschema2pojo")
public class FindingChurn {

    @JsonProperty("commit")
    private Commit commit;
    @JsonProperty("addedFindings")
    private List<Finding> addedFindings;
    @JsonProperty("findingsAddedInBranch")
    private List<Finding> findingsAddedInBranch;
    @JsonProperty("findingsInChangedCode")
    private List<Finding> findingsInChangedCode;
    @JsonProperty("removedFindings")
    private List<Finding> removedFindings;
    @JsonProperty("findingsRemovedInBranch")
    private List<Finding> findingsRemovedInBranch;

    public FindingChurn(Commit commit, List<Finding> addedFindings, List<Finding> findingsAddedInBranch, List<Finding> findingsInChangedCode, List<Finding> removedFindings, List<Finding> findingsRemovedInBranch) {
        this.commit = commit;
        this.addedFindings = addedFindings;
        this.findingsAddedInBranch = findingsAddedInBranch;
        this.findingsInChangedCode = findingsInChangedCode;
        this.removedFindings = removedFindings;
        this.findingsRemovedInBranch = findingsRemovedInBranch;
    }

    @JsonProperty("commit")
    public Commit getCommit() {
        return commit;
    }

    @JsonProperty("commit")
    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    @JsonProperty("addedFindings")
    public List<Finding> getAddedFindings() {
        return addedFindings;
    }

    @JsonProperty("findingsAddedInBranch")
    public List<Finding> getFindingsAddedInBranch() {
        return findingsAddedInBranch;
    }

    @JsonProperty("findingsInChangedCode")
    public List<Finding> getFindingsInChangedCode() {
        return findingsInChangedCode;
    }

    @JsonProperty("removedFindings")
    public List<Finding> getRemovedFindings() {
        return removedFindings;
    }

    @JsonProperty("findingsRemovedInBranch")
    public List<Finding> getFindingsRemovedInBranch() {
        return findingsRemovedInBranch;
    }
}
