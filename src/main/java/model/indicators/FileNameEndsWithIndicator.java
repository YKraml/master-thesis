package model.indicators;

import java.io.File;

public class FileNameEndsWithIndicator extends Indicator {

    private final String fileName;

    public FileNameEndsWithIndicator(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean test(File file) {
        return file.getName().endsWith(fileName);
    }
}
