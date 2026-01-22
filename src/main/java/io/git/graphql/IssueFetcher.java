package io.git.graphql;

import io.git.graphql.model.common.PageInfo;
import io.git.graphql.model.issues.Issue;
import io.git.graphql.model.issues.IssueRoot;
import jakarta.inject.Inject;
import logic.StateHandler;

import java.net.http.HttpClient;
import java.util.stream.Stream;

public class IssueFetcher extends GraphQlFetcher<IssueRoot, Issue> {

    private static final String QUERY = """
            query ($owner: String!, $name: String!, $pageSize: Int!, $cursor: String) {
              repository(owner: $owner, name: $name) {
                issues(
                  first: $pageSize,
                  after: $cursor,
                  orderBy: { field: CREATED_AT, direction: ASC }
                ) {
                  pageInfo {
                    hasNextPage
                    endCursor
                  }
                  nodes {
                    number
                    title
                    createdAt
                    state
                  }
                }
              }
            }
            """;

    @Inject
    public IssueFetcher(StateHandler stateHandler, HttpClient httpClient) {
        super(IssueRoot.class, stateHandler, httpClient);
    }

    @Override
    protected String getQuery() {
        return QUERY;
    }

    @Override
    protected Stream<Issue> getStream(IssueRoot issueRoot) {
        return issueRoot.data().repository().issues().nodes().stream();
    }

    @Override
    protected PageInfo getPageInfo(IssueRoot issueRoot) {
        return issueRoot.data().repository().issues().pageInfo();
    }
}