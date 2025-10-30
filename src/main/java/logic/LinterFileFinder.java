package logic;

import model.Linter;
import model.indicators.FileIndicator;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class LinterFileFinder {

    public Optional<Result> searchForLinter(File file, Linter linter) {

        for (FileIndicator fileIndicator : linter.getIndicators()) {
                boolean test;
                try {
                    test = fileIndicator.test(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (test) {
                    return Optional.of(new Result(file.getPath(), linter.name()));
                }
            }

        return Optional.empty();
    }

    public record Result(String fileName, String linterName) {
    }

}
