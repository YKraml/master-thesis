package io;

import model.Result;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class CsvReaderWriter {

    private File getResultFileCsv(String repoOwner, String repoName) {
        return new File("results" + File.separator + repoOwner + "." + repoName + ".csv");
    }

    public boolean resultFileCsvIsPresent(String repoOwner, String repoName) {
        return new File("results" + File.separator + repoOwner + "." + repoName + ".csv").exists();
    }

    public void writeResultAsCsv(Result result) throws IOException {
        File resultFile = getResultFileCsv(result.repoOwner(), result.repoName());
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
