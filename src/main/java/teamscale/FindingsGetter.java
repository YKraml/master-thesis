package teamscale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class FindingsGetter {

    public static void getFindings() throws IOException {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            // Prepare the GET request
            HttpGet httpget = new HttpGet(
                    "https://thesis-yannick.teamscale.dev/api/v2025.8/projects/oracle-opengrok/findings/list" +
                            "?case-insensitive-path=false&start=0&max=300"
            );

            String auth = "kraml:xcYTPCR6snDcJyWPCJgy8uJsvyoPcxpq";
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = "Basic " + new String(encodedAuth);
            httpget.setHeader("Authorization", authHeader);

            // Add headers
            httpget.addHeader("accept", "application/json");

            // Execute the request
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            // Print the response if not null
            if (entity != null) {
                try (InputStream instream = entity.getContent();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(instream))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println();
                        System.out.println(line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
