package io.json;

import jakarta.inject.Inject;
import model.writable.GitHubRepository;

public class GitHubRepositoryReaderWriter extends ReaderWriter<GitHubRepository> {

    @Inject
    protected GitHubRepositoryReaderWriter() {
        super(GitHubRepository.class);
    }

    @Override
    protected String getFolderName() {
        return "github_repos";
    }
}
