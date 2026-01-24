package io.json;

import jakarta.inject.Inject;
import logic.FilteredManager;

import java.io.File;

public class FilteredProjectLinterReaderWriter extends ReaderWriter<FilteredManager.FilteredProjectLinter> {

    @Inject
    public FilteredProjectLinterReaderWriter() {
        super(FilteredManager.FilteredProjectLinter.class);
    }

    @Override
    protected String getFolderName() {
        return "filtered" + File.separator + "filtered_linter";
    }
}
