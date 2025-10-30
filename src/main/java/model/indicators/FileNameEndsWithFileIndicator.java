package model.indicators;

import java.io.File;

public class FileNameEndsWithFileIndicator extends FileIndicator {

    private final String fileName;

    public FileNameEndsWithFileIndicator(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean test(File file) {
        return file.getName().endsWith(fileName);
    }
}
