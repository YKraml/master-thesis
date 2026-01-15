package io.json;

import jakarta.inject.Inject;
import model.CemProject;

import java.io.File;

public class CemProjectReaderWriter extends ReaderWriter<CemProject> {

    @Inject
    protected CemProjectReaderWriter() {
        super(CemProject.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        throw new UnsupportedOperationException("Only by name");
    }

    @Override
    protected String getFolderName() {
        return "temp" + File.separator + "cem";
    }
}
