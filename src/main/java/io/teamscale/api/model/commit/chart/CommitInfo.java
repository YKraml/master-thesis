package io.teamscale.api.model.commit.chart;

import java.util.List;

public record CommitInfo(String type, String branchName, Long timestamp, List<ParentCommit> parentCommits) {

}
