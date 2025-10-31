package logic;

import model.Language;
import model.Linter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinterFolderFinder {

    private final LinterFileFinder linterFileFinder;

    public LinterFolderFinder(LinterFileFinder linterFileFinder) {
        this.linterFileFinder = linterFileFinder;
    }

    public List<LinterFileFinder.Result> findLinters(File folder) {
        List<LinterFileFinder.Result> linters = new ArrayList<>();

        for (Linter linter : Linter.getLinterOfLanguage(Language.JAVA)) {
            searchForLinter(folder, linter, linters);
        }

        return linters;
    }

    private void searchForLinter(File folder, Linter linter, List<LinterFileFinder.Result> linters) {
        for (File file : getFilesInFolder(folder)) {
            Optional<LinterFileFinder.Result> result = linterFileFinder.searchForLinter(file, linter);
            if (result.isPresent()) {
                linters.add(result.get());
                return;
            }
        }
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
}
