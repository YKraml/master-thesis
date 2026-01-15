package io.json;

import jakarta.inject.Inject;
import model.CemProjectPair;
import util.Constants;

import java.io.File;

import static java.io.File.separator;

public class CemProjectPairReaderWriter extends ReaderWriter<CemProjectPair> {

    @Inject
    protected CemProjectPairReaderWriter() {
        super(CemProjectPair.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        return new File(getFolderName() + File.separator + repoOwner + "_" + repoName + ".json");
    }

    @Override
    protected String getFolderName() {
        return "temp" + separator + "cem" + separator + Constants.CURRENT_LANGUAGE.name().toLowerCase();
    }
}
