package logic;

import model.Language;
import model.Linter;
import model.indicators.FileIndicator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinterFinder {


    public LinterFinder() {
    }

    public List<Result> findLinters(File folder) {
        List<Result> linters = new ArrayList<>();

        for (Linter linter : Linter.getLinterOfLanguage(Language.JAVA)) {
            searchForLinter(folder, linter, linters);
        }

        return linters;
    }

    private void searchForLinter(File folder, Linter linter, List<Result> linters) {
        for (File file : getFilesInFolder(folder)) {
            Optional<Result> result = searchForLinter(file, linter);
            if (result.isPresent()) {
                linters.add(result.get());
                return;
            }
        }
    }

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


    private List<File> getFilesInFolder(File folder) {
        List<File> collector = new ArrayList<>();
        listFilesInFolder(folder, collector);
        return collector;
    }

    private void listFilesInFolder(File folder, List<File> collector) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesInFolder(fileEntry, collector);
            } else {
                collector.add(fileEntry);
            }
        }
    }

    public record Result(String fileName, String linterName) {
    }
}
