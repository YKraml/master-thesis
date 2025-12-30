package io;

import api.teamscale.model.cleaned.TeamscaleData;
import jakarta.inject.Inject;

import java.io.File;

public class TeamscaleDataReaderWriter extends ReaderWriter<TeamscaleData> {

    @Inject
    protected TeamscaleDataReaderWriter() {
        super(TeamscaleData.class);
    }

    @Override
    public File getFile(String repoOwner, String repoName) {
        return new File(getFolderName() + File.separator + repoOwner + "." + repoName + ".json");
    }

    @Override
    protected String getFolderName() {
        return "temp" + File.separator + "teamscale_data";
    }
}
