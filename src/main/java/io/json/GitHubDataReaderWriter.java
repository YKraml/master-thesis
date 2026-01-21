package io.json;

import model.GitHubData;
import util.Constants;

import java.io.File;

public class GitHubDataReaderWriter extends ReaderWriter<GitHubData> {

    protected GitHubDataReaderWriter() {
        super(GitHubData.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        return new File(getFolderName() + File.separator + repoOwner + "." + repoName + ".json");
    }

    @Override
    protected String getFolderName() {
        return "temp" + File.separator + "github_data" + File.separator + Constants.CURRENT_LANGUAGE.name().toLowerCase();
    }
}
