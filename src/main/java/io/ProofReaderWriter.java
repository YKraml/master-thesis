package io;

import jakarta.inject.Inject;
import model.Proof;
import util.Constants;

import java.io.File;

public class ProofReaderWriter extends ReaderWriter<Proof> {

    @Inject
    public ProofReaderWriter() {
        super(Proof.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        return new File(getFolderName() + File.separator + repoOwner + "." + repoName + ".json");
    }

    @Override
    protected String getFolderName() {
        return "temp" + File.separator + "proof" + File.separator + Constants.CURRENT_LANGUAGE.name().toLowerCase();
    }
}
