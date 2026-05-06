package dk.easv.tiffixexamweblager.GUI.Models;

import dk.easv.tiffixexamweblager.BE.ExtractedFile;
import dk.easv.tiffixexamweblager.BLL.FileImportManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * GUI model for the file-import flow.
 * Exposes an ObservableList so any TableView / ListView bound to it
 * updates automatically after a fetch.
 */
public class FileImportModel {

    private final FileImportManager manager;
    private final ObservableList<ExtractedFile> extractedFiles =
            FXCollections.observableArrayList();

    public FileImportModel() {
        manager = new FileImportManager();
    }

    /**
     * Hits the API, unpacks the ZIP, and replaces the observable list contents
     * with the freshly extracted TIFFs.
     *
     * @return the same observable list (already updated) so callers can bind once
     *         and call this method on every fetch without rebinding
     * @throws Exception propagated from network or ZIP extraction failures
     */
    public ObservableList<ExtractedFile> fetchAndExtract() throws Exception {
        extractedFiles.setAll(manager.fetchAndExtract());
        return extractedFiles;
    }

    /** The live list — bind a TableView/ListView to this once in initialize(). */
    public ObservableList<ExtractedFile> getExtractedFiles() {
        return extractedFiles;
    }
}