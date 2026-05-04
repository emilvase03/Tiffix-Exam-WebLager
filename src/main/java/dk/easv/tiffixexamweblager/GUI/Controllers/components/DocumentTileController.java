package dk.easv.tiffixexamweblager.GUI.Controllers.components;

import dk.easv.tiffixexamweblager.BE.Document;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DocumentTileController {

    @FXML private VBox root;
    @FXML private Label lblDocumentTitle;

    private Document document;

    public void setDocument(Document document) {
        this.document = document;
        lblDocumentTitle.setText("Document " + document.getSortOrder());
    }

    public VBox getRoot() {
        return root;
    }

    public Document getDocument() {
        return document;
    }
}
