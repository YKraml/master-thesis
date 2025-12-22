package api;

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

    public HttpResponse<String> getResponse(String url) throws IOException, InterruptedException {
        String auth = Constants.TEAMSCALE_USER + ":" + Constants.TEAMSCALE_ACCESS_KEY;
        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", authHeader)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }

        return response;
    }

}
