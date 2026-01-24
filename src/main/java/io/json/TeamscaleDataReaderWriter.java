package io.json;

import io.teamscale.api.model.cleaned.TeamscaleData;
import jakarta.inject.Inject;

public class TeamscaleDataReaderWriter extends ReaderWriter<TeamscaleData> {

    @Inject
    protected TeamscaleDataReaderWriter() {
        super(TeamscaleData.class);
    }

    @Override
    protected String getFolderName() {
        return "teamscale_data";
    }
}
