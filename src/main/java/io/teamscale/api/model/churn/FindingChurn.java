package io.teamscale.api.model.churn;

import java.util.List;

public record FindingChurn(
        Commit commit,
        List<Finding> addedFindings,
        List<Finding> findingsAddedInBranch,
        List<Finding> findingsInChangedCode,
        List<Finding> removedFindings,
        List<Finding> findingsRemovedInBranch) {

    public static FindingChurn empty(int time) {
        return new FindingChurn(
                new Commit("INSERTED", "INSERTED", ((long) time) * 1000),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }
}
