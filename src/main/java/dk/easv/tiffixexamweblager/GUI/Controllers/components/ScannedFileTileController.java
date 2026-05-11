package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.BLL.Utils.ImageTransformations;

// Java imports
import java.awt.image.BufferedImage;
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
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;

public class ScannedFileTileController {

    private static final double THUMB_W = 120;
    private static final double THUMB_H = 160;

    @FXML private ImageView imgThumbnail;
    @FXML private VBox root;
    @FXML private Label lblFileTitle;

    private ScannedFile file;

    public void setFile(ScannedFile file) {
        this.file = file;
        lblFileTitle.setText(extractDisplayName(file));
        renderThumbnail();
    }

    public void setScannedFile(ScannedFile file) {
        setFile(file);
    }

    public ScannedFile getFile() {
        return file;
    }


    public void refresh() {
        if (file != null) renderThumbnail();
    }

    private void renderThumbnail() {
        BufferedImage base = resolveBaseImage();
        if (base == null) {
            imgThumbnail.setImage(null);
            return;
        }

        // Composite user-level adjustments on top of the already-rule-processed image.
        // This is fast because the heavy profile-rule processing was done at fetch time.
        BufferedImage display = ImageTransformations.applyAll(
                base, file.getUserRotation(), file.getUserBrightness());

        WritableImage fxImage = SwingFXUtils.toFXImage(display, null);
        imgThumbnail.setImage(fxImage);
        imgThumbnail.setFitWidth(THUMB_W);
        imgThumbnail.setFitHeight(THUMB_H);
        imgThumbnail.setPreserveRatio(false);
        applyCenterCrop(imgThumbnail);
    }


    private BufferedImage resolveBaseImage() {
        if (file.getProcessedImage() != null) return file.getProcessedImage();

        String path = file.getFilePath();
        if (path == null || path.isBlank()) return null;

        try {
            BufferedImage raw = ImageIO.read(new File(path));
            if (raw != null) {

                file.setProcessedImage(raw);
            }
        } catch (IOException ignored) {

        }
        return file.getProcessedImage();
    }

    private String extractDisplayName(ScannedFile file) {
        return Path.of(file.getFilePath()).getFileName().toString();
    }

    //crop the image so the thumbnail is fully filled while losing as
    private void applyCenterCrop(ImageView iv) {
        Image img = iv.getImage();
        if (img == null) return;

        double imageRatio = img.getWidth() / img.getHeight();
        double thumbRatio = THUMB_W / THUMB_H;
        Rectangle2D viewport;

        if (imageRatio > thumbRatio) {
            // Image too wide → crop left/right
            double newWidth = img.getHeight() * thumbRatio;
            double x = (img.getWidth() - newWidth) / 2;
            viewport = new Rectangle2D(x, 0, newWidth, img.getHeight());
        } else {
            // Image too tall → crop top/bottom
            double newHeight = img.getWidth() / thumbRatio;
            double y = (img.getHeight() - newHeight) / 2;
            viewport = new Rectangle2D(0, y, img.getWidth(), newHeight);
        }
        iv.setViewport(viewport);
    }
}