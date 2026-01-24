package io.json;

import jakarta.inject.Inject;
import model.writable.Proof;

public class ProofReaderWriter extends ReaderWriter<Proof> {

    @Inject
    public ProofReaderWriter() {
        super(Proof.class);
    }

    @Override
    protected String getFolderName() {
        return "proof";
    }
}
