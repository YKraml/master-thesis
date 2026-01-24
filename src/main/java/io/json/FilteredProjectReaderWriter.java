package io.json;

import jakarta.inject.Inject;
import logic.FilteredManager;

import java.io.File;

public class FilteredProjectReaderWriter extends ReaderWriter<FilteredManager.FilteredProject> {

    @Inject
    public FilteredProjectReaderWriter() {
        super(FilteredManager.FilteredProject.class);
    }

    @Override
    protected String getFolderName() {
        return "filtered" + File.separator + "filtered_project";
    }
}
