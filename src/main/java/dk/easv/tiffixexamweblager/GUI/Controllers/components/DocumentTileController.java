package dk.easv.tiffixexamweblager.GUI.Controllers.components;

import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.GUI.Controllers.EmployeeDashboardController;

import java.awt.image.BufferedImage;

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

/**
 * Controller for a document tile (folder) in the document strip.
 *
 * <p><b>Label vs sort-order</b>: The tile has two independent concepts:
 * <ul>
 *   <li>{@code label} — the human-visible name ("Document 1").
 *       Set once at creation by the dashboard via {@link #setLabel(String)}.
 *       <b>Never changes</b>, even when the tile is dragged to a different position.
 *   <li>{@code sortOrder} inside the {@link Document} object — the current visual
 *       position, updated by drag-reorder. Used only for drag tokens and
 *       sessionData lookups; never shown to the user.
 * </ul>
 */
public class DocumentTileController {

    // ── FXML ─────────────────────────────────────────────────────────────────

    @FXML private VBox       root;
    @FXML private StackPane  thumbArea;
    @FXML private VBox       iconPlaceholder;
    @FXML private Label      lblIconSubtitle;
    @FXML private ImageView  imgThumbnail;
    @FXML private Label      lblDocumentTitle;   // shows the immutable creation label
    @FXML private Label      lblFileCount;

    // ── State ─────────────────────────────────────────────────────────────────

    private Document                    document;
    private EmployeeDashboardController dashboardController;
    private boolean                     selected = false;

    /**
     * The immutable display name ("Document 1").
     * Assigned once by {@link #setLabel(String)} and never derived from sortOrder.
     */
    private String label = "";

    // ── Drag-drop ─────────────────────────────────────────────────────────────

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

    // ── Public API ────────────────────────────────────────────────────────────

    public void setDashboardController(EmployeeDashboardController controller) {
        this.dashboardController = controller;
    }

    /**
     * Binds the document object to this tile.
     *
     * <p><b>This does NOT update the title label.</b> The title is set separately
     * by {@link #setLabel(String)} so it reflects the creation order and never
     * changes when the tile is dragged to a different position.
     *
     * @param document the document whose sortOrder is used for drag tokens
     */
    public void setDocument(Document document) {
        this.document = document;
        lblIconSubtitle.setText(""); // keep placeholder clean
    }

    /**
     * Sets the immutable human-visible label for this tile ("Document 1").
     *
     * <p>Call this once after {@link #setDocument}, and do <b>not</b> call it again
     * on drag-reorder. The label must not change when the tile moves.
     *
     * @param label the creation-order name, e.g. "Document 1"
     */
    public void setLabel(String label) {
        this.label = label;
        lblDocumentTitle.setText(label);
    }

    /**
     * Updates the "N pages" sub-label.
     */
    public void setFileCount(int count) {
        lblFileCount.setText(count == 0 ? "" : count + (count == 1 ? " page" : " pages"));
    }

    /**
     * Shows a first-page thumbnail. Pass {@code null} to revert to the folder icon.
     */
    public void setThumbnail(BufferedImage image) {
        if (image == null) {
            showPlaceholder();
            return;
        }
        WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
        imgThumbnail.setImage(fxImage);
        imgThumbnail.fitWidthProperty().bind(thumbArea.widthProperty());
        imgThumbnail.fitHeightProperty().bind(thumbArea.heightProperty());
        iconPlaceholder.setVisible(false);
        iconPlaceholder.setManaged(false);
        imgThumbnail.setVisible(true);
        imgThumbnail.setManaged(true);
    }

    /**
     * Highlights or un-highlights this tile.
     * Uses CSS class toggling, not inline styles, so themes work correctly.
     */
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

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Document getDocument() { return document; }
    public VBox     getRoot()     { return root; }
    public String   getLabel()    { return label; }
    public boolean  isSelected()  { return selected; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showPlaceholder() {
        imgThumbnail.setVisible(false);
        imgThumbnail.setManaged(false);
        iconPlaceholder.setVisible(true);
        iconPlaceholder.setManaged(true);
    }
}