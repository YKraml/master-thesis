package common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.apache.commons.io.FileUtils;
import teamscale.model.Result;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NormalisedResultReaderWriter {

    public void writeNormalisedResult(Result result, String linter) throws IOException {
        File resultFile = getNormalisedResultFile(result.repoOwner(), result.repoName(), linter);
        if (!resultFile.getParentFile().exists()) {
            resultFile.getParentFile().mkdirs();
        }
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }

        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(resultFile, result);
    }

    public List<Result> readAllNormalisedResults() throws IOException {
        List<Result> list = new ArrayList<>();
        for (File file : FileUtils.listFiles(new File(getFolderName()), new String[]{".json"}, false)) {
            try {
                list.add(new ObjectMapper().readValue(file, Result.class));
            } catch (MismatchedInputException e) {
                e.printStackTrace();
                file.delete();
            }

        }
        return list;
    }

    private File getNormalisedResultFile(String repoOwner, String repoName, String linter) {
        return new File(getFolderName() + File.separator + repoOwner + "." + repoName + "." + linter + ".json");

    }

    private static String getFolderName() {
        return "normalised";
    }

    public boolean resultFileIsPresent(String repoOwner, String repoName, String linter) {
        return getNormalisedResultFile(repoOwner, repoName, linter).exists();
    }

}
