package logic;

import model.Linter;
import model.Proof;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinterFolderFinder {

    private final LinterFileFinder linterFileFinder;

    public LinterFolderFinder(LinterFileFinder linterFileFinder) {
        this.linterFileFinder = linterFileFinder;
    }


    public Map<Linter, Proof> findLinters(File folder) {

        Map<Linter, Proof> linters = new HashMap<>();
        for (File file : getFilesForFolder(folder)) {
            Map<Linter, Proof> lintersInFile = linterFileFinder.searchForLinters(file);
            linters.putAll(lintersInFile);
        }

        return linters;
    }

    private List<File> getFilesForFolder(File folder) {
        List<File> collector = new ArrayList<>();
        listFilesForFolder(folder, collector);
        return collector;
    }

    public void listFilesForFolder(File folder, List<File> collector) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, collector);
            } else {
                collector.add(fileEntry);
            }
        }
    }
}
