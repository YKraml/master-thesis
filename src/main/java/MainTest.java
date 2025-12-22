

import com.google.inject.Guice;
import com.google.inject.Injector;
import graphql.IssuesFetcher;
import graphql.StarFetcher;
import graphql.model.issues.Issue;
import graphql.model.stars.Star;
import org.w3c.dom.Node;
import util.GuiceModule;

import java.util.List;

public class MainTest {

    public static void main(String[] args) throws Exception {

        Injector injector = Guice.createInjector(new GuiceModule());
        IssuesFetcher issuesFetcher = injector.getInstance(IssuesFetcher.class);
        StarFetcher starFetcher = injector.getInstance(StarFetcher.class);

        String owner = "cubiq";
        String repo = "ComfyUI_Workflows";

        List<Issue> issues = issuesFetcher.getIssuesOf(owner, repo);
        System.out.println(issues.size());
        System.out.println(issues);

        List<Star> stars = starFetcher.getStarsOf(owner, repo);
        System.out.println(stars.size());
        System.out.println(stars);

    }

}
