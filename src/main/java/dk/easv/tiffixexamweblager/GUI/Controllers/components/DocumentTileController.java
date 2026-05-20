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

import java.util.List;
import java.util.function.Consumer;

public class DocumentTileController {

    private double TILE_WIDTH;
    private double TILE_HEIGHT;

    private static final double FLOW_PADDING  = 16.0;
    private static final String DRAG_OVER_CLASS = "drag-over";
    private static final String SELECTED_CLASS  = "selected";
    private static final String EXPANDED_CLASS  = "expanded";

    @FXML private VBox      root;
    @FXML private VBox      tileContent;
    @FXML private FlowPane  inlineFilesPane;
    @FXML private StackPane thumbArea;
    @FXML private ImageView imgThumbnail;
    @FXML private ImageView docIcon;
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
            cc.putString("DOC");
            db.setContent(cc);
            if (dashboardController != null)
                dashboardController.setDraggedDocument(document);
            tileContent.setOpacity(0.5);
            e.consume();
        });

        tileContent.setOnDragDone(e -> {
            tileContent.setOpacity(1.0);
            removeDragHighlight();
            if (dashboardController != null)
                dashboardController.setDraggedDocument(null);
        });

        tileContent.setOnDragOver(e -> {
            if (e.getDragboard().hasString()) {
                String s = e.getDragboard().getString();
                if (s.equals("DOC") || s.equals("FILE"))
                    e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        tileContent.setOnDragEntered(e -> {
            if (e.getDragboard().hasString()) {
                String s = e.getDragboard().getString();
                if (s.equals("DOC")) {
                    Document dragged = dashboardController != null
                            ? dashboardController.getDraggedDocument() : null;
                    if (dragged != null && dragged != document)
                        addDragHighlight();
                } else if (s.equals("FILE")) {
                    addDragHighlight();
                }
            }
            e.consume();
        });

        tileContent.setOnDragExited(e -> { removeDragHighlight(); e.consume(); });

        tileContent.setOnDragDropped(e -> {
            removeDragHighlight();
            Dragboard db = e.getDragboard();
            boolean success = false;

            if (db.hasString() && dashboardController != null) {
                String s = db.getString();

                if (s.equals("DOC")) {
                    // Move document: insert dragged before this document
                    Document dragged = dashboardController.getDraggedDocument();
                    if (dragged != null && dragged != document) {
                        dashboardController.moveDocument(dragged, document);
                        success = true;
                    }

                } else if (s.equals("FILE")) {
                    ScannedFile draggedFile = dashboardController.getDraggedFile();
                    if (draggedFile != null) {
                        dashboardController.moveFileToDocument(draggedFile, document);
                        success = true;
                    }
                }
            }

            e.setDropCompleted(success);
            e.consume();
        });
    }

    public void setContainerWidthProperty(ReadOnlyDoubleProperty widthProperty) {
        this.containerWidth = widthProperty;
    }

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
        String name = file.getFileName();
        if (name != null && !name.isBlank()) return name;
        return "File " + file.getScanOrder();
    }

    private void addDragHighlight() {
        if (!tileContent.getStyleClass().contains(DRAG_OVER_CLASS))
            tileContent.getStyleClass().add(DRAG_OVER_CLASS);
    }

    private void removeDragHighlight() {
        tileContent.getStyleClass().remove(DRAG_OVER_CLASS);
    }



    public void setDashboardController(EmployeeDashboardController c) {
        this.dashboardController = c; }

    public void setDocument(Document document) {
        this.document = document; }

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
            if (!tileContent.getStyleClass().contains(SELECTED_CLASS))
                tileContent.getStyleClass().add(SELECTED_CLASS);
        } else {
            tileContent.getStyleClass().remove(SELECTED_CLASS);
        }
    }

    public Document getDocument() {
        return document; }

    public VBox     getRoot()     {
        return root;     }

    public String   getLabel()    {
        return label;    }

    public boolean  isExpanded()  {
        return expanded; }
}