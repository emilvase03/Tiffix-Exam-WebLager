package dk.easv.tiffixexamweblager.DAL.API;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScanResult;

// Java imports
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Calls GET /getRandomFile on the WebLager API.
 * This class returns the raw ZIP bytes wrapped in a ScanResult;
 * ZipExtractor in FileImportManager unwraps them.
 */
public class FileApiClient {

    private static final String ENDPOINT =
            "https://studentiffapi-production.up.railway.app/getRandomFile";

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private final HttpClient httpClient;

    public FileApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    // Fetches one ZIP from the scanner API.
    public ScanResult fetchScanZip() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<byte[]> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new Exception("Scanner API returned HTTP " + response.statusCode());
        }

        byte[] zipBytes = response.body();
        if (zipBytes == null || zipBytes.length == 0) {
            throw new Exception("Scanner API returned an empty response.");
        }

        // Resolve a display name from Content-Disposition if present
        String fileName = response.headers()
                .firstValue("content-disposition")
                .map(FileApiClient::extractFileName)
                .orElse("scan_" + System.currentTimeMillis() + ".zip");

        return new ScanResult(fileName, zipBytes);
    }

    private static String extractFileName(String disposition) {
        int idx = disposition.indexOf("filename=");
        if (idx < 0) return "scan.zip";
        return disposition.substring(idx + 9)
                .replace("\"", "")
                .replace("UTF-8''", "")
                .trim();
    }
}