package io.teamscale.api.model.churn;

import java.util.List;

public record FindingChurn(
        Commit commit,
        List<Finding> addedFindings,
        List<Finding> findingsAddedInBranch,
        List<Finding> findingsInChangedCode,
        List<Finding> removedFindings,
        List<Finding> findingsRemovedInBranch) {
}
