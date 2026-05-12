package dk.easv.tiffixexamweblager.GUI.Controllers.components;

//Project imports
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.GUI.Controllers.EmployeeDashboardController;

//Java/JavaFX imports
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
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


    private Document                    document;
    private EmployeeDashboardController dashboardController;
    private boolean                     selected = false;

    /**
     * The immutable display name ("Document 1").
     * Assigned once by {@link #setLabel(String)} and never derived from sortOrder.
     */
    private String label = "";

    @FXML
    private void initialize() {

        // Drag source — document being moved to another position
        root.setOnDragDetected(e -> {
            if (document == null) return;
            Dragboard db = root.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            // Use sortOrder as the drag token so swapDocuments() can look up the document
            content.putString("DOC_ID:" + document.getSortOrder());
            db.setContent(content);
            root.setOpacity(0.5);
            e.consume();
        });

        root.setOnDragDone(e -> root.setOpacity(1.0));

        // Accept FILE (move to document) and DOC (reorder) drops
        root.setOnDragOver(e -> {
            if (e.getDragboard().hasString()) {
                String s = e.getDragboard().getString();
                if (s.startsWith("FILE_ID:") || s.startsWith("DOC_ID:")) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
            }
            e.consume();
        });

        root.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                String s = db.getString();

                if (s.startsWith("FILE_ID:")) {
                    // File tile dropped here → move file into this document
                    int scanOrder = Integer.parseInt(s.substring(8));
                    if (dashboardController != null) {
                        dashboardController.moveFileToDocument(scanOrder, document);
                    }
                    success = true;

                } else if (s.startsWith("DOC_ID:")) {
                    // Document tile dropped here → swap positions
                    int draggedSortOrder = Integer.parseInt(s.substring(7));
                    if (dashboardController != null) {
                        dashboardController.swapDocuments(draggedSortOrder, document);
                    }
                    success = true;
                }
            }

            e.setDropCompleted(success);
            e.consume();
        });
    }

    public void setDashboardController(EmployeeDashboardController controller) {
        this.dashboardController = controller;
    }

    public void setDocument(Document document) {
        this.document = document;
        lblIconSubtitle.setText(""); // keep placeholder clean
    }

    public void setLabel(String label) {
        this.label = label;
        lblDocumentTitle.setText(label);
    }

    // Updates the "N pages" sub-label.

    public void setFileCount(int count) {
        lblFileCount.setText(count == 0 ? "" : count + (count == 1 ? " page" : " pages"));
    }


    //Highlights or un-highlights this tile.

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            if (!root.getStyleClass().contains("selected")) {
                root.getStyleClass().add("selected");
            }
        } else {
            root.getStyleClass().remove("selected");
        }
    }

    public Document getDocument() {
        return document; }

    public VBox     getRoot()     {
        return root; }

    public String   getLabel()    {
        return label; }

    public boolean  isSelected()  {
        return selected; }

    private void showPlaceholder() {
        imgThumbnail.setVisible(false);
        imgThumbnail.setManaged(false);
        iconPlaceholder.setVisible(true);
        iconPlaceholder.setManaged(true);
    }
}