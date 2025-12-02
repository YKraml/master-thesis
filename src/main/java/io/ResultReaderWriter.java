package io;

import jakarta.inject.Inject;
import model.Result;

import java.io.File;

public class ResultReaderWriter extends ReaderWriter<Result> {

    @Inject
    public ResultReaderWriter() {
        super(Result.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        String folderName = getFolderName();
        return new File(folderName + File.separator + repoOwner + "." + repoName + ".json");
    }

    @Override
    protected String getFolderName() {
        return "temp" + File.separator + "results";
    }
}
