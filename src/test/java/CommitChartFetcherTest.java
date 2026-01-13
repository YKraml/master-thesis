import com.google.inject.Guice;
import com.google.inject.Injector;
import io.teamscale.api.CommitChartFetcher;
import io.teamscale.api.model.commit.chart.CommitChart;
import io.teamscale.api.model.commit.chart.Commits;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import util.GuiceModule;

import java.util.List;

@Disabled
public class CommitChartFetcherTest {

    private CommitChartFetcher commitChartFetcher;

    @BeforeEach
    public void setup() {
        Injector injector = Guice.createInjector(new GuiceModule());
        commitChartFetcher = injector.getInstance(CommitChartFetcher.class);
    }

    @Test
    void testAndytillErlyberly() {
        assertCommitSize("andytill", "erlyberly", 205);
    }

    @Test
    void testApacheHop() {
        assertCommitSize("apache", "hop", 918);
    }

    @Test
    void testOpenJdkJmh() {
        assertCommitSize("openjdk", "jmh", 926);
    }

    private void assertCommitSize(String repoOwner, String repoName, int expected) {
        CommitChart commitChart = commitChartFetcher.getCommitChart(repoOwner, repoName);
        List<Commits> commits = commitChart
                .getCommits()
                .stream()
                .flatMap(userWithCommits -> userWithCommits.getCommits().stream())
                .toList();
        Assertions.assertEquals(expected, commits.size());
    }

}
