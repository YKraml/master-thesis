package io.teamscale.api.model.cleaned;

import model.ProjectWritable;

import java.util.List;

public record TeamscaleData(String repoOwner, String repoName,
                            List<TeamscaleDataEntry> entries) implements ProjectWritable {

}
