package dk.easv.tiffixexamweblager.GUI.Models;

import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.BLL.BoxManager;
import dk.easv.tiffixexamweblager.BLL.DocumentManager;
import dk.easv.tiffixexamweblager.BLL.ScannedFileManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class DocumentModel {

    private final BoxManager boxManager;
    private final DocumentManager documentManager;
    private final ScannedFileManager scannedFileManager;

    private final ObservableList<Document> documentsForBox =
            FXCollections.observableArrayList();

    private final ObservableList<ScannedFile> filesForDocument =
            FXCollections.observableArrayList();

    public DocumentModel() throws Exception {
        boxManager = new BoxManager();
        documentManager = new DocumentManager();
        scannedFileManager = new ScannedFileManager();
    }


    public List<Box> getAllBoxes() throws Exception {
        return boxManager.getAllBoxes();
    }

    public ObservableList<Document> loadDocumentsForBox(Box box) throws Exception {
        documentsForBox.setAll(
                documentManager.getDocumentsForBox(box.getId())
        );
        return documentsForBox;
    }

    public ObservableList<Document> getDocumentsForBox() {
        return documentsForBox;
    }

    public ObservableList<ScannedFile> loadFilesForDocument(Document document) throws Exception {
        filesForDocument.setAll(
                scannedFileManager.getFilesForDocument(document.getId())
        );
        return filesForDocument;
    }

    public ObservableList<ScannedFile> getFilesForDocument() {
        return filesForDocument;
    }
}