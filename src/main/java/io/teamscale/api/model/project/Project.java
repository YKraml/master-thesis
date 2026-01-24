package io.teamscale.api.model.project;

import java.util.List;

public record Project(String internalId, String externalStorageProjectMappingId, String name, List<String> publicIds,
                      Long creationTimestamp, Boolean deleting, Boolean reanalyzing, Boolean copyingData,
                      Boolean configurationCompleted) {

}
