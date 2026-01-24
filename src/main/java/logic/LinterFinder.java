package logic;

import jakarta.inject.Inject;
import model.linting.FileIndicator;
import model.linting.Linter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import util.Constants;

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

        for (Linter linter : Linter.getLinterOfLanguage(Constants.CURRENT_LANGUAGE)) {
            searchForLinter(folder, linter, linters);
        }

        return linters;
    }

    public Optional<Result> searchForLinter(File folder, File file, Linter linter) throws IOException {
        for (FileIndicator fileIndicator : linter.getIndicators()) {
            if (fileIndicator.test(file)) {
                return Optional.of(new Result(folder.toPath().relativize(file.toPath()).toString(), linter.name()));
            }
        }
        return Optional.empty();
    }

    private void searchForLinter(File folder, Linter linter, List<Result> linters) throws IOException {
        List<File> files = new ArrayList<>(FileUtils.listFiles(folder, null, true));
        String[] extensions = Linter.getExtensionsOfLanguage(linter.getLanguage());

        files.sort(Comparator.comparingInt((File file) -> file.getAbsolutePath().length()).thenComparing(File::getAbsolutePath));

        for (File file : files) {

            Strings.CI.endsWithAny(file.getName(), extensions);

            Optional<Result> result = searchForLinter(folder, file, linter);
            if (result.isPresent()) {
                linters.add(result.get());
                return;
            }
        }
    }

    public record Result(String fileName, String linterName) {
    }
}
