package logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import model.Result;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResultReaderWriter {

    @Inject
    public ResultReaderWriter() {
    }

    public void writeResult(Result result) throws IOException {
        File resultFile = getResultFile(result.getRepoOwner(), result.getRepoName());
        if (!resultFile.getParentFile().exists()) {
            resultFile.getParentFile().mkdirs();
        }
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(resultFile, result);
    }

    public File getResultFile(String owner, String repoName) {
        return new File("proofs" + File.separator + owner + "." + repoName + ".json");
    }

    public List<Result> readAllResults() throws IOException {
        List<Result> list = new ArrayList<>();
        for (File file : FileUtils.listFiles(new File("proofs"), new String[]{".json"}, false)) {
            list.add(new ObjectMapper().readValue(file, Result.class));
        }
        return list;
    }
}
