package common;

import teamscale.model.Result;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ResultReaderWriter {

    public void writeResult(Result result) throws IOException {
        File resultFile = getResultFile(result.getRepoOwner(), result.getRepoName());
        if (!resultFile.getParentFile().exists()) {
            resultFile.getParentFile().mkdirs();
        }
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }

        try (PrintWriter pw = new PrintWriter(resultFile)) {
            for (Result.Entry entry : result.getEntries()) {
                String line = entry.time() + "," + entry.sourceLinesOfCode() + "," + entry.numberOfCodeClones() + "," + entry.numberOfFileSizeViolations() + "," + entry.numberOfMethodLengthViolations() + "," + entry.numberOfNestingDepthViolations();
                pw.println(line);
            }
        }
    }

    private File getResultFile(String owner, String repoName) {
        return new File("results" + File.separator + owner + "." + repoName + ".csv");
    }
}
