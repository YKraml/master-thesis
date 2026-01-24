package io.json;

import jakarta.inject.Inject;
import model.writable.CemProjectPair;

public class CemProjectPairReaderWriter extends ReaderWriter<CemProjectPair> {

    @Inject
    protected CemProjectPairReaderWriter() {
        super(CemProjectPair.class);
    }

    @Override
    protected String getFolderName() {
        return "cem_pairs";
    }
}
