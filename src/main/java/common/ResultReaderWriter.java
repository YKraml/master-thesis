package common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.apache.commons.io.FileUtils;
import teamscale.model.Result;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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

    public List<Result> readAllResults() throws IOException {
        List<Result> list = new ArrayList<>();
        String folderName = getFolderName();

        for (File file : FileUtils.listFiles(new File(folderName), new String[]{".json"}, false)) {
            try {
                list.add(new ObjectMapper().readValue(file, Result.class));
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
        return "results";
    }

    public boolean resultFileIsPresent(String repoOwner, String repoName) {
        return getResultFile(repoOwner, repoName).exists();
    }

}
