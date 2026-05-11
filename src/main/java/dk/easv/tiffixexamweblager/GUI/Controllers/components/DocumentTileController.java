package dk.easv.tiffixexamweblager.GUI.Controllers.components;

import dk.easv.tiffixexamweblager.BE.Document;

import java.awt.image.BufferedImage;

import dk.easv.tiffixexamweblager.GUI.Controllers.EmployeeDashboardController;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DocumentTileController {

    @FXML private VBox       root;
    @FXML private StackPane  thumbArea;
    @FXML private VBox       iconPlaceholder;
    @FXML private Label      lblIconSubtitle;
    @FXML private ImageView  imgThumbnail;
    @FXML private Label      lblDocumentTitle;
    @FXML private Label      lblFileCount;

    private EmployeeDashboardController dashboardController;

    private Document document;
    private boolean  selected = false;

    @FXML
    private void initialize() {

        root.setOnDragDetected(e -> {
            Dragboard db = root.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("DOC:" + document.getSortOrder());
            db.setContent(content);
            e.consume();
        });
        root.setOnDragOver(e -> {
            if (e.getDragboard().hasString()
                    && e.getDragboard().getString().startsWith("DOC:")) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        root.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasString()) {

                int draggedOrder =
                        Integer.parseInt(db.getString().substring(4));

               // EmployeeDashboardController.swapDocuments(draggedOrder, document);
            }

            e.setDropCompleted(true);
            e.consume();
        });
    }
    public void setDashboardController(EmployeeDashboardController controller) {
        this.dashboardController = controller;
    }

    public void setDocument(Document document) {
        this.document = document;

        String title = "Document " + document.getSortOrder();
        lblDocumentTitle.setText(title);
        lblIconSubtitle.setText("");
    }

    public void setFileCount(int count) {
        lblFileCount.setText(count == 0 ? "" : count + (count == 1 ? " page" : " pages"));
    }

    public Document getDocument() {
        return document; }

    public VBox     getRoot()     {
        return root; }

    private void showPlaceholder() {
        imgThumbnail.setVisible(false);
        imgThumbnail.setManaged(false);
        iconPlaceholder.setVisible(true);
        iconPlaceholder.setManaged(true);
    }
}
