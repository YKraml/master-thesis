package util;

import jakarta.inject.Inject;
import model.CemProject;

import java.util.List;

public class CemCommitsUtil {

    @Inject
    public CemCommitsUtil() {

    }

    public int calcNumberOfCommitsInAnalysisWindow(List<CemProject.Commit> commits, CemProject.Commit treatedCommit) {
        int numberOfCommitsInAnalysisWindow = 0;
        for (CemProject.Commit commit : commits) {
            if (Math.abs(commit.time() - treatedCommit.time()) < Constants.ANALYSIS_WINDOW_IN_SECONDS) {
                numberOfCommitsInAnalysisWindow++;
            }
        }

        return numberOfCommitsInAnalysisWindow;
    }

}
