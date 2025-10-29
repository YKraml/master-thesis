package logic;

import model.Linter;
import model.Proof;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LinterFolderFinder {

    private final LinterFileFinder linterFileFinder;

    public LinterFolderFinder(LinterFileFinder linterFileFinder) {
        this.linterFileFinder = linterFileFinder;
    }


    public Map<Linter, Proof> findLinters(File folder) {
        Map<Linter, Proof> linters = new ConcurrentHashMap<>();
        getFilesInFolder(folder).stream().parallel().map(linterFileFinder::searchForLinters).forEach(linters::putAll);

        return linters;
    }

    private List<File> getFilesInFolder(File folder) {
        List<File> collector = new ArrayList<>();
        listFilesInFolder(folder, collector);
        return collector;
    }

    public void listFilesInFolder(File folder, List<File> collector) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesInFolder(fileEntry, collector);
            } else {
                collector.add(fileEntry);
            }
        }
    }
}
