import api.graphql.ForkFetcher;
import api.graphql.IssueFetcher;
import api.graphql.StarFetcher;
import api.graphql.model.forks.Fork;
import api.graphql.model.issues.Issue;
import api.graphql.model.stars.Star;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.GuiceModule;

import java.util.List;

public class GraphQlTest {

    private ForkFetcher forkFetcher;
    private IssueFetcher issueFetcher;
    private StarFetcher starFetcher;

    @BeforeEach
    public void setup() {
        Injector injector = Guice.createInjector(new GuiceModule());
        issueFetcher = injector.getInstance(IssueFetcher.class);
        starFetcher = injector.getInstance(StarFetcher.class);
        forkFetcher = injector.getInstance(ForkFetcher.class);
    }

    @Test
    void test() {
        String owner = "cubiq";
        String repo = "ComfyUI_Workflows";

        List<Fork> forks = forkFetcher.fetch(owner, repo);
        List<Issue> issues = issueFetcher.fetch(owner, repo);
        List<Star> stars = starFetcher.fetch(owner, repo);

        Assertions.assertTrue(forks.size() >= 54);
        Assertions.assertTrue(issues.size() >= 4);
        Assertions.assertTrue(stars.size() >= 672);
    }
}
