package common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResultReaderWriter {

    @Inject
    public ResultReaderWriter() {
    }

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

    public Optional<Result> getResultOfRepo(String repoOwner, String repoName) throws IOException {
        File proofs = new File("proofs" + File.separator + repoOwner + "." + repoName + ".json");
        if(!proofs.exists()){
            return Optional.empty();
        }
        return Optional.ofNullable(new ObjectMapper().readValue(proofs, Result.class));
    }
}
