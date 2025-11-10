package common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.inject.Inject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProofReaderWriter {

    @Inject
    public ProofReaderWriter() {
    }

    public void writeResult(Proof proof) throws IOException {
        File resultFile = getResultFile(proof.repoOwner(), proof.repoName());
        if (!resultFile.getParentFile().exists()) {
            resultFile.getParentFile().mkdirs();
        }
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(resultFile, proof);
    }

    public File getResultFile(String owner, String repoName) {
        return new File("proofs" + File.separator + owner + "." + repoName + ".json");
    }

    public List<Proof> readAllResults() throws IOException {
        List<Proof> list = new ArrayList<>();
        for (File file : FileUtils.listFiles(new File("proofs"), new String[]{".json"}, false)) {
            try {
                list.add(new ObjectMapper().readValue(file, Proof.class));
            } catch (MismatchedInputException e){
                e.printStackTrace();
                file.delete();
            }

        }
        return list;
    }

    public Optional<Proof> getResultOfRepo(String repoOwner, String repoName) throws IOException {
        File proofs = new File("proofs" + File.separator + repoOwner + "." + repoName + ".json");
        if(!proofs.exists()){
            return Optional.empty();
        }
        return Optional.ofNullable(new ObjectMapper().readValue(proofs, Proof.class));
    }
}
