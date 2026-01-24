package io.teamscale.api;

import jakarta.inject.Inject;
import util.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HttpHelperTeamscale {

    private final HttpClient httpClient;

    @Inject
    public HttpHelperTeamscale(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpResponse<String> executeGetRequest(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(createUrl(path))
                .header("Authorization", getAuthHeader()).header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> executePostRequest(String path, String requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(createUrl(path))
                .header("Authorization", getAuthHeader())
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> executeDeleteRequest(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(createUrl(path))
                .header("Authorization", getAuthHeader())
                .header("Accept", "*/*")
                .DELETE()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static URI createUrl(String path) {
        return URI.create(Constants.TEAMSCALE_URL + path);
    }

    private static String getAuthHeader() {
        String auth = Constants.TEAMSCALE_USER + ":" + Constants.TEAMSCALE_ACCESS_KEY;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1));
    }
}
