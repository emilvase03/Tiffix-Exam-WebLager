package dk.easv.tiffixexamweblager.GUI.Controllers.components;

//Project imports
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.GUI.Controllers.EmployeeDashboardController;

//Java/JavaFX imports
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

public class DocumentTileController {

    private static final String DRAG_OVER_CLASS = "drag-over";

    @FXML private VBox      root;
    @FXML private Label     lblIconSubtitle;
    @FXML private Label     lblDocumentTitle;
    @FXML private Label     lblFileCount;

    private Document                    document;
    private EmployeeDashboardController dashboardController;
    private boolean                     selected = false;
    private String                      label    = "";

    @FXML
    private void initialize() {

        root.setOnDragDetected(e -> {
            if (document == null) return;
            Dragboard db = root.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString("DOC_ID:" + document.getSortOrder());
            db.setContent(cc);
            root.setOpacity(0.5);
            e.consume();
        });

        root.setOnDragDone(e -> {
            root.setOpacity(1.0);
            removeDragHighlight();
        });

        root.setOnDragOver(e -> {
            if (e.getDragboard().hasString()) {
                String s = e.getDragboard().getString();
                if (s.startsWith("FILE_ID:") || s.startsWith("DOC_ID:")) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
            }
            e.consume();
        });

        // Highlight when a compatible drag enters this tile
        root.setOnDragEntered(e -> {
            if (e.getDragboard().hasString()) {
                String s = e.getDragboard().getString();
                if (s.startsWith("FILE_ID:") || s.startsWith("DOC_ID:")) {
                    addDragHighlight();
                }
            }
            e.consume();
        });

        // Remove highlight when drag leaves
        root.setOnDragExited(e -> {
            removeDragHighlight();
            e.consume();
        });

        root.setOnDragDropped(e -> {
            removeDragHighlight();
            Dragboard db = e.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                String s = db.getString();
                if (s.startsWith("FILE_ID:")) {
                    int scanOrder = Integer.parseInt(s.substring(8));
                    if (dashboardController != null)
                        dashboardController.moveFileToDocument(scanOrder, document);
                    success = true;
                } else if (s.startsWith("DOC_ID:")) {
                    int draggedSortOrder = Integer.parseInt(s.substring(7));
                    if (dashboardController != null)
                        dashboardController.swapDocuments(draggedSortOrder, document);
                    success = true;
                }
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }

    private void addDragHighlight() {
        if (!root.getStyleClass().contains(DRAG_OVER_CLASS))
            root.getStyleClass().add(DRAG_OVER_CLASS);
    }

    private void removeDragHighlight() {
        root.getStyleClass().remove(DRAG_OVER_CLASS);
    }


    public void setDashboardController(EmployeeDashboardController c) { this.dashboardController = c; }

    public void setDocument(Document document) {
        this.document = document;
        lblIconSubtitle.setText("");
    }

    public void setLabel(String label) {
        this.label = label;
        lblDocumentTitle.setText(label);
    }

    public void setFileCount(int count) {
        lblFileCount.setText(count == 0 ? "" : count + (count == 1 ? " file" : " files"));
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            if (!root.getStyleClass().contains("selected")) root.getStyleClass().add("selected");
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

}