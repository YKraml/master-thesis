package io.json;

import io.teamscale.api.model.cleaned.TeamscaleData;
import jakarta.inject.Inject;
import util.Constants;

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
        return "temp" + File.separator + "teamscale_data" + File.separator + Constants.CURRENT_LANGUAGE.name().toLowerCase();
    }
}
