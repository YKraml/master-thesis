package io.json;

import jakarta.inject.Inject;
import model.CombinedData;

import java.io.File;

public class CombinedDataReaderWriter extends ReaderWriter<CombinedData> {

    @Inject
    public CombinedDataReaderWriter() {
        super(CombinedData.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        return new File(getFolderName() + File.separator + repoOwner + "." + repoName + ".json");
    }

    @Override
    protected String getFolderName() {
        return "temp" + File.separator + "combined_data";
    }
}
