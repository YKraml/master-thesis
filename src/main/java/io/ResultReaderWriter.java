package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import model.Result;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResultReaderWriter {

    public void writeResult(Result result) throws IOException {
        File resultFile = getResultFile(result.repoOwner(), result.repoName());
        if (!resultFile.getParentFile().exists()) {
            resultFile.getParentFile().mkdirs();
        }
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }

        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(resultFile, result);
    }

    public List<Pair<String, Result>> readAllResults() throws IOException {
        List<Pair<String, Result>> list = new ArrayList<>();
        for (File file : FileUtils.listFiles(new File(getFolderName()), new String[]{".json"}, false)) {
            try {
                Result result = new ObjectMapper().readValue(file, Result.class);
                list.add(Pair.of(file.getName().split("\\.")[2], result));
            } catch (MismatchedInputException e) {
                e.printStackTrace();
                file.delete();
            }
        }
        return list;
    }

    private File getResultFile(String repoOwner, String repoName) {
        String folderName = getFolderName();
        return new File(folderName + File.separator + repoOwner + "." + repoName + ".json");
    }

    private static String getFolderName() {
        return "temp" + File.separator + "results";
    }

    public boolean resultFileIsPresent(String repoOwner, String repoName) {
        return getResultFile(repoOwner, repoName).exists();
    }

    public Result getResult(String repoOwner, String repoName) throws IOException {
        return new ObjectMapper().readValue(getResultFile(repoOwner, repoName), Result.class);
    }
}
