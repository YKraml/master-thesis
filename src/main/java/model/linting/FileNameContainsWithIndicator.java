package model.linting;

import org.apache.commons.lang3.Strings;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.File;
import java.io.IOException;

public class FileNameContainsWithIndicator extends FileIndicator {

    private final String fileName;

    public FileNameContainsWithIndicator(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean test(File file) throws IOException {
        return Strings.CI.contains(file.getName(), fileName);
    }

    @Override
    public boolean test(DiffEntry.ChangeType changeType, String diffText) {
        return Strings.CI.contains(diffText, fileName);
    }
}
