package io.teamscale.api.model.cleaned;

import model.writable.Writable;

import java.util.List;

public record TeamscaleData(String repoOwner, String repoName,
                            List<TeamscaleDataEntry> entries) implements Writable {

}
