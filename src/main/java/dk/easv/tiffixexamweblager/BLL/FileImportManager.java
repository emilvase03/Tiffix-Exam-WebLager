package dk.easv.tiffixexamweblager.BLL;

import dk.easv.tiffixexamweblager.BE.ScanResult;
import dk.easv.tiffixexamweblager.DAL.API.FileApiClient;
import dk.easv.tiffixexamweblager.DAL.API.ZipExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates fetching a ZIP from the external API and extracting its TIFFs.
 * Deliberately knows nothing about Documents, Boxes, or persistence —
 * that is the responsibility of whoever consumes the returned ExtractedFile list.
 */

public class FileImportManager {

    private final FileApiClient apiClient = new FileApiClient();

    /**
     * Fetches a ZIP file from the external API, extracts all TIFF files found inside it,
     * and converts each TIFF into a {@link ScanResult}.
     *
     * @return one ScanResult per TIFF found in the ZIP; may be empty if none are present
     * @throws Exception if the API fetch fails or the ZIP cannot be processed
     */

    public List<ScanResult> importScansFromApi() throws Exception {

        ScanResult zipResult = apiClient.fetchScanZip();

        ZipExtractor extractor = new ZipExtractor();
        List<ZipExtractor.ExtractedFile> extracted =
                extractor.extractTiffs(zipResult.fileBytes());

        List<ScanResult> results = new ArrayList<>();


        for (ZipExtractor.ExtractedFile file : extracted) {
            results.add(new ScanResult(
                    file.getFileName(),
                    file.getFileBytes()
            ));
        }

        return results;
    }
}