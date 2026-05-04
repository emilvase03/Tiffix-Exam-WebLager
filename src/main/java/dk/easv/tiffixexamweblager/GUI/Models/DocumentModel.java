package dk.easv.tiffixexamweblager.GUI.Models;

import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.BLL.BoxManager;
import dk.easv.tiffixexamweblager.BLL.DocumentManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class DocumentModel {

    private final BoxManager boxManager;
    private final DocumentManager documentManager;

    private final ObservableList<Document> documentsForBox = FXCollections.observableArrayList();

    public DocumentModel() throws Exception {
        boxManager = new BoxManager();
        documentManager = new DocumentManager();
    }

    public List<Box> getAllBoxes() throws Exception {
        return boxManager.getAllBoxes();
    }

    public ObservableList<Document> loadDocumentsForBox(Box box) throws Exception {
        documentsForBox.setAll(documentManager.getDocumentsForBox(box.getId()));
        return documentsForBox;
    }
}