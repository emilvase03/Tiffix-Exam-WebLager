package dk.easv.tiffixexamweblager.GUI.Controllers.components;

import dk.easv.tiffixexamweblager.BE.ScannedFile;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ScannedFileTileController {

    private static final double THUMB_W = 120;
    private static final double THUMB_H = 160;

    @FXML private ImageView imgThumbnail;
    @FXML private VBox root;
    @FXML private Label lblFileTitle;

    private ScannedFile file;

    // Called for both DB and newly scanned files
    public void setFile(ScannedFile file) {
        this.file = file;
        lblFileTitle.setText(extractDisplayName(file));
        loadThumbnail(file.getFilePath());
    }

    //  for fetched ,unsaved files
    public void setScannedFile(ScannedFile file) {
        setFile(file);
    }

    public ScannedFile getFile() {
        return file;
    }

    private String extractDisplayName(ScannedFile file) {
        return Path.of(file.getFilePath()).getFileName().toString();
    }

    private void loadThumbnail(String filePath) {
        try {
            BufferedImage buffered = ImageIO.read(new File(filePath));
            if (buffered == null) {
                imgThumbnail.setImage(null);
                return;
            }

            WritableImage fxImage = SwingFXUtils.toFXImage(buffered, null);
            imgThumbnail.setImage(fxImage);

            // Thumbnail size
            imgThumbnail.setFitWidth(THUMB_W);
            imgThumbnail.setFitHeight(THUMB_H);
            imgThumbnail.setPreserveRatio(false);

            applyCenterCrop(imgThumbnail);

        } catch (IOException e) {
            imgThumbnail.setImage(null);
        }
    }

    /**
     * Crops the image so the thumbnail is fully filled
     * while losing as little content as possible.
     */
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
