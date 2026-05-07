package dk.easv.tiffixexamweblager.GUI.Models;

import dk.easv.tiffixexamweblager.BE.ScanResult;
import dk.easv.tiffixexamweblager.BLL.FileImportManager;
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

    /**
     * Fetches scans from API (ZIP → TIFFs) and appends all results.
     */
    public void fetchScansFromApi() throws Exception {

        List<ScanResult> results = manager.importScansFromApi();
        scanResults.addAll(results);
    }

    /**
     * Clears between documents / barcode splits.
     */
    public void clear() {
        scanResults.clear();
    }

    public ObservableList<ScanResult> getScanResults() {
        return scanResults;
    }
}