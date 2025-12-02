package io;

import jakarta.inject.Inject;
import model.ProofWithResult;

import java.io.File;

public class ProofWithResultReaderWriter extends ReaderWriter<ProofWithResult> {

    @Inject
    public ProofWithResultReaderWriter() {
        super(ProofWithResult.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        return new File(getFolderName() + File.separator + repoOwner + "." + repoName + ".json");
    }

    @Override
    protected String getFolderName() {
        return "temp" + File.separator + "all";
    }
}
