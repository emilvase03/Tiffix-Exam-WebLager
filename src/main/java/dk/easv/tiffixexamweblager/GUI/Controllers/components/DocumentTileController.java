package dk.easv.tiffixexamweblager.GUI.Controllers.components;

import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.GUI.Controllers.EmployeeDashboardController;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class DocumentTileController {


    private double TILE_WIDTH;
    private double TILE_HEIGHT;

    private static final double FLOW_PADDING = 16.0;

    private static final String DRAG_OVER_CLASS = "drag-over";
    private static final String SELECTED_CLASS  = "selected";
    private static final String EXPANDED_CLASS  = "expanded";

    @FXML private VBox      root;
    @FXML private VBox      tileContent;
    @FXML private FlowPane  inlineFilesPane;
    @FXML private Label     lblDocumentTitle;
    @FXML private Label     lblFileCount;

    private Document                    document;
    private EmployeeDashboardController dashboardController;
    private boolean                     selected = false;
    private boolean                     expanded = false;
    private String                      label    = "";

    private ReadOnlyDoubleProperty containerWidth;


    @FXML
    private void initialize() {
        TILE_WIDTH  = root.getPrefWidth();
        TILE_HEIGHT = tileContent.getPrefHeight();

        tileContent.setOnDragDetected(e -> {
            if (document == null) return;
            Dragboard db = tileContent.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString("DOC_ID:" + document.getSortOrder());
            db.setContent(cc);
            tileContent.setOpacity(0.5);
            e.consume();
        });

        tileContent.setOnDragDone(e -> { tileContent.setOpacity(1.0); removeDragHighlight(); });

        tileContent.setOnDragOver(e -> {
            if (e.getDragboard().hasString()) {
                String s = e.getDragboard().getString();
                if (s.startsWith("FILE_ID:") || s.startsWith("DOC_ID:"))
                    e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        tileContent.setOnDragEntered(e -> {
            if (e.getDragboard().hasString()) {
                String s = e.getDragboard().getString();
                if (s.startsWith("FILE_ID:") || s.startsWith("DOC_ID:")) addDragHighlight();
            }
            e.consume();
        });

        tileContent.setOnDragExited(e -> { removeDragHighlight(); e.consume(); });

        tileContent.setOnDragDropped(e -> {
            removeDragHighlight();
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String s = db.getString();
                if (s.startsWith("FILE_ID:")) {
                    int scanOrder = Integer.parseInt(s.substring(8));
                    if (dashboardController != null) dashboardController.moveFileToDocument(scanOrder, document);
                    success = true;
                } else if (s.startsWith("DOC_ID:")) {
                    int draggedOrder = Integer.parseInt(s.substring(7));
                    if (dashboardController != null) dashboardController.swapDocuments(draggedOrder, document);
                    success = true;
                }
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }

    // ── Container width binding ───────────────────────────────────────────────

    public void setContainerWidthProperty(ReadOnlyDoubleProperty widthProperty) {
        this.containerWidth = widthProperty;
    }

    // ── Expand / Collapse ─────────────────────────────────────────────────────

    public void setExpanded(boolean expand) {
        this.expanded = expand;

        if (expand) {
            double fullWidth = containerWidth != null
                    ? Math.max(TILE_WIDTH, containerWidth.get() - FLOW_PADDING)
                    : TILE_WIDTH * 3;
            root.setPrefWidth(fullWidth);

            if (containerWidth != null) {
                containerWidth.addListener((obs, oldW, newW) -> {
                    if (this.expanded)
                        root.setPrefWidth(Math.max(TILE_WIDTH, newW.doubleValue() - FLOW_PADDING));
                });
            }

            inlineFilesPane.setVisible(true);
            inlineFilesPane.setManaged(true);

            if (!tileContent.getStyleClass().contains(EXPANDED_CLASS))
                tileContent.getStyleClass().add(EXPANDED_CLASS);

        } else {
            // Restore to FXML-declared tile width
            root.setPrefWidth(TILE_WIDTH);
            inlineFilesPane.setVisible(false);
            inlineFilesPane.setManaged(false);
            inlineFilesPane.getChildren().clear();
            tileContent.getStyleClass().remove(EXPANDED_CLASS);
        }
    }

    public void setInlineFiles(List<ScannedFile> files, Consumer<ScannedFile> onSelect) {
        inlineFilesPane.getChildren().clear();

        if (files == null || files.isEmpty()) {
            Label empty = new Label("No files yet");
            empty.getStyleClass().add("text-muted");
            inlineFilesPane.getChildren().add(empty);
            return;
        }

        for (ScannedFile file : files) {
            VBox miniTile = buildMiniFileTile(file);
            miniTile.setOnMouseClicked(e -> {
                if (onSelect != null) onSelect.accept(file);
                e.consume();
            });
            inlineFilesPane.getChildren().add(miniTile);
        }
    }

    private VBox buildMiniFileTile(ScannedFile file) {
        FontIcon icon = new FontIcon("bi-file-earmark");
        icon.getStyleClass().add("mini-file-icon");

        Label name = new Label(resolveFileName(file));
        name.getStyleClass().add("mini-file-label");
        name.setMaxWidth(72);
        name.setWrapText(false);

        VBox tile = new VBox(4, icon, name);
        tile.setAlignment(Pos.TOP_CENTER);
        tile.getStyleClass().add("mini-file-tile");
        tile.setPrefWidth(80);
        return tile;
    }

    private String resolveFileName(ScannedFile file) {
        String p = file.getFilePath();
        if (p != null && !p.isBlank()) {
            try { return Path.of(p).getFileName().toString(); } catch (Exception ignored) {}
        }
        return "File " + file.getScanOrder();
    }

    private void addDragHighlight() {
        if (!tileContent.getStyleClass().contains(DRAG_OVER_CLASS))
            tileContent.getStyleClass().add(DRAG_OVER_CLASS);
    }

    private void removeDragHighlight() { tileContent.getStyleClass().remove(DRAG_OVER_CLASS); }

    public void setDashboardController(EmployeeDashboardController c) {
        this.dashboardController = c; }

    public void setDocument(Document document) {
        this.document = document; }

    public void setLabel(String label) {
        this.label = label;
        lblDocumentTitle.setText(label);
    }


    public Document getDocument() {
        return document; }

    public VBox     getRoot()     {
        return root;      }

    public String   getLabel()    {
        return label;     }

}