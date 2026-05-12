package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.BLL.Utils.ImageTransformations;
import dk.easv.tiffixexamweblager.GUI.Controllers.EmployeeDashboardController;

// Java imports
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

// JavaFX imports
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;

public class ScannedFileTileController {

    private static final double THUMB_W = 120;
    private static final double THUMB_H = 160;

    @FXML private ImageView imgThumbnail;
    @FXML private VBox      root;
    @FXML private Label     lblFileTitle;

    private ScannedFile                  file;
    private EmployeeDashboardController  dashboardController;

    public void setFile(ScannedFile file) {
        this.file = file;
        lblFileTitle.setText(buildDisplayName(file));
        renderThumbnail();
    }

    public void setScannedFile(ScannedFile file) { setFile(file); }
    public ScannedFile getFile()                 { return file; }

    public void setDashboardController(EmployeeDashboardController controller) {
        this.dashboardController = controller;
    }

    public void refresh() {
        if (file != null) renderThumbnail();
    }

    @FXML
    private void initialize() {
        root.setOnDragDetected(e -> {
            if (file == null) return;
            Dragboard db = root.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("FILE_ID:" + file.getScanOrder());
            db.setContent(content);
            root.setOpacity(0.5);
            e.consume();
        });

        root.setOnDragDone(e -> root.setOpacity(1.0));

        root.setOnDragOver(e -> {
            String token = e.getDragboard().getString();
            if (e.getDragboard().hasString() && token.startsWith("FILE_ID:")) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        root.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasString() && db.getString().startsWith("FILE_ID:")) {
                int draggedScanOrder = Integer.parseInt(db.getString().substring(8));
                if (dashboardController != null) {
                    dashboardController.reorderFiles(draggedScanOrder, file);
                }
                e.setDropCompleted(true);
            } else {
                e.setDropCompleted(false);
            }
            e.consume();
        });
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void renderThumbnail() {
        BufferedImage base = resolveBaseImage();
        if (base == null) { imgThumbnail.setImage(null); return; }

        BufferedImage display = ImageTransformations.applyAll(
                base, file.getUserRotation(), file.getUserBrightness());

        WritableImage fxImage = SwingFXUtils.toFXImage(display, null);
        imgThumbnail.setImage(fxImage);
        imgThumbnail.setFitWidth(THUMB_W);
        imgThumbnail.setFitHeight(THUMB_H);
        imgThumbnail.setPreserveRatio(false);
        applyCenterCrop(imgThumbnail);
    }

    /**
     * Returns the processed image, decoding it if not yet cached.
     * Prefers the in-memory processedImage, then falls back to the TIFF bytes
     * (covers both freshly-scanned and DB-loaded files), then the temp file path
     * as a last resort.
     */
    private BufferedImage resolveBaseImage() {
        if (file.getProcessedImage() != null) return file.getProcessedImage();

        // Primary source: raw TIFF bytes (always available for DB-loaded files)
        byte[] bytes = file.getTiffFile();
        if (bytes != null && bytes.length > 0) {
            try {
                BufferedImage raw = ImageIO.read(new ByteArrayInputStream(bytes));
                if (raw != null) { file.setProcessedImage(raw); return raw; }
            } catch (IOException ignored) { }
        }

        // Fallback: temp file on disk (freshly scanned, bytes somehow null)
        String path = file.getFilePath();
        if (path != null && !path.isBlank()) {
            try {
                BufferedImage raw = ImageIO.read(new File(path));
                if (raw != null) { file.setProcessedImage(raw); return raw; }
            } catch (IOException ignored) { }
        }

        return null;
    }

    /**
     * Shows the original file name when scanning (filePath is set),
     * or "Page N" for DB-loaded files.
     */
    private String buildDisplayName(ScannedFile file) {
        String path = file.getFilePath();
        if (path != null && !path.isBlank()) {
            return Path.of(path).getFileName().toString();
        }
        return "Page " + file.getScanOrder();
    }

    // ── Center-crop ───────────────────────────────────────────────────────────

    private void applyCenterCrop(ImageView iv) {
        Image img = iv.getImage();
        if (img == null) return;

        double imageRatio = img.getWidth() / img.getHeight();
        double thumbRatio = THUMB_W / THUMB_H;
        Rectangle2D viewport;

        if (imageRatio > thumbRatio) {
            double newWidth = img.getHeight() * thumbRatio;
            double x = (img.getWidth() - newWidth) / 2;
            viewport = new Rectangle2D(x, 0, newWidth, img.getHeight());
        } else {
            double newHeight = img.getWidth() / thumbRatio;
            double y = (img.getHeight() - newHeight) / 2;
            viewport = new Rectangle2D(0, y, img.getWidth(), newHeight);
        }

        iv.setViewport(viewport);
    }
}