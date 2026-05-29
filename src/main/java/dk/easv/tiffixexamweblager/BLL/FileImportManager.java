package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScanResult;
import dk.easv.tiffixexamweblager.DAL.API.FileApiClient;
import dk.easv.tiffixexamweblager.BLL.Utils.ZipExtractor;

// Java imports
import java.util.ArrayList;
import java.util.List;

public class FileImportManager {
    private final FileApiClient apiClient = new FileApiClient();

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