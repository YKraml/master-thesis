package io.json;

import jakarta.inject.Inject;
import model.CemProject;
import util.Constants;

import java.io.File;

import static java.io.File.separator;

public class CemProjectTreatmentReaderWriter extends ReaderWriter<CemProject> {

    @Inject
    protected CemProjectTreatmentReaderWriter() {
        super(CemProject.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        return new File(getFolderName() + File.separator + "linter." + repoOwner + "." + repoName + ".json");
    }

    @Override
    protected String getFolderName() {
        return "temp" + separator + "cem" + separator + Constants.CURRENT_LANGUAGE.name().toLowerCase() + separator + "treated";
    }
}
