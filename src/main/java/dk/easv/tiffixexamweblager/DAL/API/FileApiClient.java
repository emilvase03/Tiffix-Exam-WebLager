package dk.easv.tiffixexamweblager.DAL.API;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin HTTP client for the Tiffix file API.
 * Returns raw bytes so the caller decides what to do with the file.
 */
public class FileApiClient {

    private static final String ENDPOINT = "https://studentiffapi-production.up.railway.app/getRandomFile/";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient;

    public FileApiClient() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    /**
     * Fetches a random file from the API.
     *
     * @return raw file bytes
     * @throws IOException          if the network request fails
     * @throws InterruptedException if the thread is interrupted while waiting
     * @throws Exception            if the server returns a non-200 status
     */
    public byte[] fetchRandomFile() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new Exception("File API returned HTTP " + response.statusCode());
        }

        return response.body();
    }
}