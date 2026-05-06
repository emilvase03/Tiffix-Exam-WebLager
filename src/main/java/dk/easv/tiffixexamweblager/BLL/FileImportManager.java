package dk.easv.tiffixexamweblager.BLL;

import dk.easv.tiffixexamweblager.BE.ExtractedFile;
import dk.easv.tiffixexamweblager.DAL.API.FileApiClient;
import dk.easv.tiffixexamweblager.DAL.API.ZipExtractor;

import java.util.List;

/**
 * Orchestrates fetching a ZIP from the external API and extracting its TIFFs.
 * Deliberately knows nothing about Documents, Boxes, or persistence —
 * that is the responsibility of whoever consumes the returned ExtractedFile list.
 */
public class FileImportManager {

    private final FileApiClient fileApiClient;
    private final ZipExtractor  zipExtractor;

    public FileImportManager() {
        fileApiClient = new FileApiClient();
        zipExtractor  = new ZipExtractor();
    }

    /**
     * Fetches a ZIP from the API and returns all TIFF files found inside it.
     *
     * @return one ExtractedFile per TIFF entry in the ZIP, never empty
     * @throws Exception if the network call fails, the response is not a valid ZIP,
     *                   or the ZIP contains no TIFF files
     */
    public List<ExtractedFile> fetchAndExtract() throws Exception {
        byte[] zipBytes = fileApiClient.fetchRandomFile();
        return zipExtractor.extractTiffs(zipBytes);
    }
}