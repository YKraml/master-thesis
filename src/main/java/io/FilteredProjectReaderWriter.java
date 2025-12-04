package io;

import jakarta.inject.Inject;
import logic.FilteredManager;
import util.Constants;

import java.io.File;

public class FilteredProjectReaderWriter extends ReaderWriter<FilteredManager.FilteredProject> {

    @Inject
    public FilteredProjectReaderWriter() {
        super(FilteredManager.FilteredProject.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        return new File(getFolderName() + File.separator + "project." + repoOwner + "." + repoName + ".json");
    }

    @Override
    protected String getFolderName() {
        return "temp" + File.separator + "filtered" + File.separator + Constants.CURRENT_LANGUAGE.name().toLowerCase();
    }
}
