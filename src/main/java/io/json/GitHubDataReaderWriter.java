package io.json;

import model.writable.GitHubData;

public class GitHubDataReaderWriter extends ReaderWriter<GitHubData> {

    protected GitHubDataReaderWriter() {
        super(GitHubData.class);
    }

    @Override
    protected String getFolderName() {
        return "github_data";
    }
}
