package io.json;

import jakarta.inject.Inject;
import model.CemProject;
import util.Constants;

import java.io.File;

import static java.io.File.separator;

public class CemProjectReaderWriter extends ReaderWriter<CemProject> {

    @Inject
    protected CemProjectReaderWriter() {
        super(CemProject.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        return new File(getFolderName() + File.separator + repoOwner + "_" + repoName + ".json");
    }

    @Override
    protected String getFolderName() {
        return "temp" + separator + "cem_projects" + separator + Constants.CURRENT_LANGUAGE.name().toLowerCase();
    }
}
