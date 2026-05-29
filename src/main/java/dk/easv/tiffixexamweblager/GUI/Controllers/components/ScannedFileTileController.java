package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.BLL.Utils.ImageTransformations;
import dk.easv.tiffixexamweblager.GUI.Controllers.EmployeeDashboardController;

// Java imports
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ScannedFileTileController {

    private static final String DRAG_OVER_CLASS = "drag-over";

    @FXML private ImageView imgThumbnail;
    @FXML private VBox      root;
    @FXML private Label     lblFileTitle;

    private ScannedFile                 file;
    private EmployeeDashboardController dashboardController;

    public void setFile(ScannedFile file) {
        this.file = file;
        lblFileTitle.setText(buildDisplayName(file));
        renderThumbnail();
    }

    public void setScannedFile(ScannedFile file) {
        setFile(file); }

    public ScannedFile getFile() {
        return file; }

    public void setDashboardController(EmployeeDashboardController c) {
        this.dashboardController = c;
    }

    public void refresh() {
        if (file != null) {
            lblFileTitle.setText(buildDisplayName(file));
            renderThumbnail();
        }
    }

    @FXML
    private void initialize() {

        root.setOnDragDetected(e -> {
            Dragboard db = root.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();
            content.putString("FILE");
            db.setContent(content);

            if (dashboardController != null) {
                dashboardController.setDraggedFile(file);
            }

            e.consume();
        });

        root.setOnDragDone(e -> {
            root.setOpacity(1.0);
            removeDragHighlight();
        });

        root.setOnDragOver(e -> {
            if (e.getDragboard().hasString() &&
                    e.getDragboard().getString().equals("FILE")) {

                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        root.setOnDragEntered(e -> {
            if (e.getDragboard().hasString() &&
                    e.getDragboard().getString().equals("FILE")) {

                addDragHighlight();
            }
            e.consume();
        });

        root.setOnDragExited(e -> {
            removeDragHighlight();
            e.consume();
        });

        root.setOnDragDropped(e -> {
            removeDragHighlight();

            Dragboard db = e.getDragboard();

            if (db.hasString() && db.getString().equals("FILE")) {

                if (dashboardController != null) {
                    dashboardController.reorderFiles(
                            dashboardController.getDraggedFile(),
                            file
                    );
                }

                e.setDropCompleted(true);
            } else {
                e.setDropCompleted(false);
            }
            e.consume();
        });
    }

    private void renderThumbnail() {
        BufferedImage base = resolveBaseImage();
        if (base == null) { imgThumbnail.setImage(null); return; }

        BufferedImage display = ImageTransformations.applyAll(
                base, file.getUserRotation(), file.getUserBrightness());

        WritableImage fxImage = SwingFXUtils.toFXImage(display, null);
        imgThumbnail.setImage(fxImage);
        applyCenterCrop(imgThumbnail);
    }

    private BufferedImage resolveBaseImage() {
        if (file.getProcessedImage() != null) return file.getProcessedImage();

        byte[] bytes = file.getTiffFile();
        if (bytes != null && bytes.length > 0) {
            try {
                BufferedImage raw = ImageIO.read(new ByteArrayInputStream(bytes));
                if (raw != null) { file.setProcessedImage(raw); return raw; }
            } catch (IOException ignored) { }
        }

        String path = file.getFilePath();
        if (path != null && !path.isBlank()) {
            try {
                BufferedImage raw = ImageIO.read(new File(path));
                if (raw != null) { file.setProcessedImage(raw); return raw; }
            } catch (IOException ignored) { }
        }

        return null;
    }

    private String buildDisplayName(ScannedFile file) {

        String name = file.getFileName();
        if (name != null && !name.isBlank()) return name;

        String path = file.getFilePath();
        if (path != null && !path.isBlank()) {
            try { return Path.of(path).getFileName().toString(); }
            catch (Exception ignored) { }
        }

        return "File " + file.getScanOrder();
    }

    private void applyCenterCrop(ImageView iv) {
        Image img = iv.getImage();
        if (img == null) return;

        double thumbW = iv.getFitWidth();
        double thumbH = iv.getFitHeight();
        double imageRatio = img.getWidth() / img.getHeight();
        double thumbRatio = thumbW / thumbH;
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

    private void addDragHighlight() {
        if (!root.getStyleClass().contains(DRAG_OVER_CLASS))
            root.getStyleClass().add(DRAG_OVER_CLASS);
    }

    private void removeDragHighlight() {
        root.getStyleClass().remove(DRAG_OVER_CLASS); }
}