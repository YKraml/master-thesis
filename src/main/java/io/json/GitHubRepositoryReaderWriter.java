package io.json;

import jakarta.inject.Inject;
import model.GitHubRepository;
import util.Constants;

import java.io.File;

public class GitHubRepositoryReaderWriter extends ReaderWriter<GitHubRepository> {

    @Inject
    protected GitHubRepositoryReaderWriter() {
        super(GitHubRepository.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        return new File(getFolderName() + File.separator + repoOwner + "." + repoName + ".json");
    }

    @Override
    protected String getFolderName() {
        return "temp" + File.separator + "github_repos" + File.separator + Constants.CURRENT_LANGUAGE.name().toLowerCase();
    }
}
