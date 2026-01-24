package io.teamscale.api.model.commit.chart;

import java.util.List;

public record Commits(String revision, CommitInfo commit, String message, String author, String repositoryIdentifier,
                      List<String> commitTypes, String mail, Integer numAddedFiles, Integer numChangedFiles,
                      Integer numDeletedFiles) {

}
