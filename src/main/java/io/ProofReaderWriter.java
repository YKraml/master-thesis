package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.inject.Inject;
import model.Proof;
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

    public void writeProof(Proof proof) throws IOException {
        File proofFile = getProofFile(proof.repoOwner(), proof.repoName());
        if (!proofFile.getParentFile().exists()) {
            proofFile.getParentFile().mkdirs();
        }
        if (!proofFile.exists()) {
            proofFile.createNewFile();
        }
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(proofFile, proof);
    }

    public File getProofFile(String repoOwner, String repoName) {
        return new File("temp" + File.separator + "proofs" + File.separator + repoOwner + "." + repoName + ".json");
    }

    public List<Proof> readAllProofs() throws IOException {
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

    public Optional<Proof> getProofOfRepo(String repoOwner, String repoName) throws IOException {
        File proofs = getProofFile(repoOwner, repoName);
        if(!proofs.exists()){
            return Optional.empty();
        }
        return Optional.ofNullable(new ObjectMapper().readValue(proofs, Proof.class));
    }
}
