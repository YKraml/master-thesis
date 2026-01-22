package io.git.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.git.graphql.model.common.PageInfo;
import jakarta.inject.Inject;
import logic.StateHandler;
import org.apache.commons.lang3.tuple.Pair;
import util.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class GraphQlFetcher<Root, Target> {

    // rate limiting code
    private static final long MIN_REQUEST_INTERVAL_MS = 720;
    private static final Object RATE_LIMIT_LOCK = new Object();
    private static long nextAvailableRequestTime = System.currentTimeMillis();

    private final Class<Root> clazz;
    private final StateHandler stateHandler;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper; // Optimization: Reuse Mapper

    @Inject
    protected GraphQlFetcher(Class<Root> clazz, StateHandler stateHandler, HttpClient httpClient) {
        this.stateHandler = stateHandler;
        this.clazz = clazz;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public List<Target> fetch(String repoOwner, String repoName) {
        try {
            return getNonSafe(repoOwner, repoName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected List<Target> getNonSafe(String repoOwner, String repoName) throws Exception {
        List<Root> allRoots = new ArrayList<>();

        String cursor = null;
        boolean hasNextPage = true;
        int pageNumber = 1;

        while (hasNextPage) {
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Fetching %s from GraphQL. Page: %d".formatted(clazz.getSimpleName(), pageNumber));

            String requestBody = buildRequestBody(repoOwner, repoName, cursor);
            HttpResponse<String> response = getResponse(requestBody);
            Root root = objectMapper.readValue(response.body(), clazz);

            allRoots.add(root);
            PageInfo pageInfo = getPageInfo(root);
            hasNextPage = pageInfo.hasNextPage();
            cursor = pageInfo.endCursor();
            pageNumber++;
        }

        return allRoots.stream().flatMap(this::getStream).toList();
    }

    protected abstract String getQuery();

    protected abstract Stream<Target> getStream(Root root);

    protected abstract PageInfo getPageInfo(Root root);

    private String buildRequestBody(String owner, String repo, String cursor) throws Exception {
        ObjectNode variables = objectMapper.createObjectNode()
                .put("owner", owner)
                .put("name", repo)
                .put("pageSize", 100)
                .put("cursor", cursor);

        JsonNode root = objectMapper.createObjectNode()
                .put("query", getQuery())
                .set("variables", variables);

        return objectMapper.writeValueAsString(root);
    }

    private HttpResponse<String> getResponse(String requestBody) throws IOException, InterruptedException {
        // rate limiting code
        synchronized (RATE_LIMIT_LOCK) {
            long currentTime = System.currentTimeMillis();
            if (currentTime < nextAvailableRequestTime) {
                long waitTime = nextAvailableRequestTime - currentTime;
                Thread.sleep(waitTime);
            }
            nextAvailableRequestTime = System.currentTimeMillis() + MIN_REQUEST_INTERVAL_MS;
        }
        // rate limiting code end

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create("https://api.github.com/graphql"))
                .header("Authorization", "Bearer " + Constants.GITHUB_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("GitHub API error: " + response.body());
        }
        return response;
    }
}