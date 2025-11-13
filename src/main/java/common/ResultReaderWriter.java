package common;

import teamscale.model.Result;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

public class ResultReaderWriter {

    public void writeResult(Result result) throws IOException {
        File resultFile = getResultFile(result.repoOwner(), result.repoName());
        if (!resultFile.getParentFile().exists()) {
            resultFile.getParentFile().mkdirs();
        }
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }

        try (PrintWriter pw = new PrintWriter(resultFile)) {
            pw.println(Result.Entry.csvHeader());
            for (Result.Entry entry : result.entries()) {
                pw.println(entry.csvLine());
            }
        }
    }

    private File getResultFile(String repoOwner, String repoName) {
        return new File("results" + File.separator + repoOwner + "." + repoName + ".csv");
    }

    public boolean resultIsPresent(String repoOwner, String repoName) {
        return new File("results" + File.separator + repoOwner + "." + repoName + ".csv").exists();
    }
}
