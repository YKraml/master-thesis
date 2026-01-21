package model;

import io.git.graphql.model.forks.Fork;
import io.git.graphql.model.issues.Issue;

import java.util.List;

public record GitHubData(String repoOwner,
                         String repoName,
                         List<Fork> forks,
                         List<Issue> issues) implements ProjectWritable {
}
