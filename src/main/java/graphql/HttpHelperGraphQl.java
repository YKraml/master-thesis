package graphql;

import jakarta.inject.Inject;
import util.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpHelperGraphQl {

    private final HttpClient httpClient;

    @Inject
    public HttpHelperGraphQl(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpResponse<String> getResponse(String requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/graphql"))
                .header("Authorization", "Bearer " + Constants.GITHUB_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("GitHub API error: " + response.body());
        }
        return response;
    }
}
