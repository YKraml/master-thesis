package logic;

import jakarta.inject.Inject;
import model.Language;
import model.Linter;
import model.FileIndicator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LinterFinder {

    @Inject
    public LinterFinder() {
    }

    public List<Result> findLinters(File folder) throws IOException {
        List<Result> linters = new ArrayList<>();

        for (Linter linter : Linter.getLinterOfLanguage(Language.JAVA)) {
            searchForLinter(folder, linter, linters);
        }

        return linters;
    }

    private void searchForLinter(File folder, Linter linter, List<Result> linters) throws IOException {
        List<File> files = new ArrayList<>(FileUtils.listFiles(folder, Linter.getExtensionsOfLanguage(linter.getLanguage()), true));
        files.sort(Comparator.comparingInt((File file) -> file.getAbsolutePath().length()).thenComparing(File::getAbsolutePath));

        for (File file : files) {
            Optional<Result> result = searchForLinter(folder, file, linter);
            if (result.isPresent()) {
                linters.add(result.get());
                return;
            }
        }
    }

    public Optional<Result> searchForLinter(File folder, File file, Linter linter) throws IOException {
        for (FileIndicator fileIndicator : linter.getIndicators()) {
            if (fileIndicator.test(file)) {
                return Optional.of(new Result(folder.toPath().relativize(file.toPath()).toString(), linter.name()));
            }
        }
        return Optional.empty();
    }

    public record Result(String fileName, String linterName) {
    }
}
