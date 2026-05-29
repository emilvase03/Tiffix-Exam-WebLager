package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScanResult;
import dk.easv.tiffixexamweblager.BLL.FileImportManager;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class FileImportModel {

    private final FileImportManager manager;
    private final ObservableList<ScanResult> scanResults =
            FXCollections.observableArrayList();

    public FileImportModel() {
        manager = new FileImportManager();
    }

    public void fetchScansFromApi() throws Exception {

        List<ScanResult> results = manager.importScansFromApi();
        scanResults.addAll(results);
    }

    public void clear() {
        scanResults.clear();
    }

    public ObservableList<ScanResult> getScanResults() {
        return scanResults;
    }
}