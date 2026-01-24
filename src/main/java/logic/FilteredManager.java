package logic;

import io.json.FilteredProjectLinterReaderWriter;
import io.json.FilteredProjectReaderWriter;
import jakarta.inject.Inject;
import model.linting.Linter;
import model.writable.Writable;

import java.io.IOException;

public class FilteredManager {

    private final FilteredProjectLinterReaderWriter filteredProjectLinterReaderWriter;
    private final FilteredProjectReaderWriter filteredProjectReaderWriter;

    @Inject
    public FilteredManager(FilteredProjectLinterReaderWriter filteredProjectLinterReaderWriter, FilteredProjectReaderWriter filteredProjectReaderWriter) {
        this.filteredProjectLinterReaderWriter = filteredProjectLinterReaderWriter;
        this.filteredProjectReaderWriter = filteredProjectReaderWriter;
    }

    public void filterProjectLinter(String repoOwner, String repoName, Linter linter, String reason, String proof) throws IOException {
        filteredProjectLinterReaderWriter.write(new FilteredProjectLinter(repoOwner, repoName, linter, reason, proof));
    }

    public void filterCompleteProject(String repoOwner, String repoName, String reason, String proof) throws IOException {
        filteredProjectReaderWriter.write(new FilteredProject(repoOwner, repoName, reason, proof));
    }

    public record FilteredProject(String repoOwner, String repoName, String reason, String proof) implements Writable {
    }

    public record FilteredProjectLinter(String repoOwner, String repoName, Linter linter, String reason,
                                        String proof) implements Writable {
    }
}
