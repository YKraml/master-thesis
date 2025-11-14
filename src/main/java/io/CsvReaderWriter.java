package io;

import model.Result;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class CsvReaderWriter {

    private File getResultFileCsv(String repoOwner, String repoName, String linter) {
        return new File("python/root/csv" + File.separator + repoOwner + "." + repoName + "." + linter + ".csv");
    }

    public void writeResultAsCsv(Result result, String linter) throws IOException {
        File resultFile = getResultFileCsv(result.repoOwner(), result.repoName(), linter);
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

}
