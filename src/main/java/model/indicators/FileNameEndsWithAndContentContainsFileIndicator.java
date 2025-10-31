package model.indicators;

import org.eclipse.jgit.diff.DiffEntry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileNameEndsWithAndContentContainsFileIndicator extends FileIndicator {

    private final String fileName;
    private final String fileContent;

    public FileNameEndsWithAndContentContainsFileIndicator(String fileName, String fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    @Override
    public boolean test(File file) throws IOException {
        return file.getName().endsWith(fileName) && Files.readString(file.toPath()).contains(fileContent);
    }

    @Override
    public boolean test(DiffEntry.ChangeType changeType, String diffText) {
        return diffText.contains(fileName) && diffText.contains(fileContent);
    }


}
