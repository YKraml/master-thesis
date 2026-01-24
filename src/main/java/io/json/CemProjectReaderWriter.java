package io.json;

import jakarta.inject.Inject;
import model.writable.CemProject;

public class CemProjectReaderWriter extends ReaderWriter<CemProject> {

    @Inject
    protected CemProjectReaderWriter() {
        super(CemProject.class);
    }

    @Override
    protected String getFolderName() {
        return "cem_projects";
    }
}
