package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.BLL.BoxManager;
import dk.easv.tiffixexamweblager.BLL.DocumentManager;
import dk.easv.tiffixexamweblager.BLL.ScannedFileManager;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class BoxDocumentModel {
    private final BoxManager boxManager;
    private final DocumentManager documentManager;
    private final ScannedFileManager scannedFileManager;
    private final ObservableList<Box> allBoxes = FXCollections.observableArrayList();
    private final ObservableList<Document> documentsForBox = FXCollections.observableArrayList();
    private final ObservableList<ScannedFile> filesForDocument = FXCollections.observableArrayList();

    public BoxDocumentModel() throws Exception {
        boxManager = new BoxManager();
        documentManager = new DocumentManager();
        scannedFileManager = new ScannedFileManager();
    }

    // BoxManager
    public ObservableList<Box> getAllObservableIncludingSoftDeleted() throws Exception {
        allBoxes.setAll(boxManager.getAllIncludingSoftDeleted());
        return allBoxes;
    }

    public Box createBox(Box box) throws Exception {
        Box created = boxManager.createBox(box);
        insertSorted(created);
        return created;
    }

    public void deleteBox(Box box) throws Exception {
        boxManager.deleteBox(box);
        allBoxes.remove(box);
    }

    private void insertSorted(Box box) {
        int i = 0;
        while (i < allBoxes.size() && allBoxes.get(i).getNumber() < box.getNumber()) {
            i++;
        }
        allBoxes.add(i, box);
    }

    // DocumentManager
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

    public ObservableList<ScannedFile> getFilesForDocument() {
        return filesForDocument;
    }

    // ScannedFileManager
    public ObservableList<ScannedFile> loadFilesForDocument(Document document) throws Exception {
        filesForDocument.setAll(
                scannedFileManager.getFilesForDocument(document.getId())
        );
        return filesForDocument;
    }
}
