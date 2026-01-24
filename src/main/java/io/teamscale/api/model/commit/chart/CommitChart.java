package io.teamscale.api.model.commit.chart;

import java.util.List;

public record CommitChart(Long startTimestamp, Long endTimestamp, List<UserWithCommits> commits) {

}
