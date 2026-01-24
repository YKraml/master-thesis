package io.teamscale.api.model.commit.chart;

import java.util.List;

public record UserWithCommits(String fullName, List<Commits> commits) {

}
