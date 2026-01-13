package io.json;

import jakarta.inject.Inject;
import logic.FilteredManager;
import util.Constants;

import java.io.File;

public class FilteredProjectLinterReaderWriter extends ReaderWriter<FilteredManager.FilteredProjectLinter> {

    @Inject
    public FilteredProjectLinterReaderWriter() {
        super(FilteredManager.FilteredProjectLinter.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        return new File(getFolderName() + File.separator + "linter." + repoOwner + "." + repoName + ".json");
    }

    @Override
    public File getFile(String id) {
        throw new UnsupportedOperationException("Only repoOwner and repoName can be used");
    }

    @Override
    protected String getFolderName() {
        return "temp" + File.separator + "filtered" + File.separator + Constants.CURRENT_LANGUAGE.name().toLowerCase();
    }
}
