package model.indicators;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileNameEndsWithAndContentContainsIndicator extends Indicator {

    private final String fileName;
    private final String fileContent;

    public FileNameEndsWithAndContentContainsIndicator(String fileName, String fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    @Override
    public boolean test(File file) throws IOException {
        return file.getName().endsWith(fileName) && Files.readString(file.toPath()).contains(fileContent);
    }
}
