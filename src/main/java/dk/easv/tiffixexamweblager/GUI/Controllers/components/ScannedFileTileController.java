package dk.easv.tiffixexamweblager.GUI.Controllers.components;

import dk.easv.tiffixexamweblager.BE.ScannedFile;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ScannedFileTileController {

    @FXML private VBox root;
    @FXML private Label lblFileTitle;

    private ScannedFile file;

    public void setFile(ScannedFile file) {
        this.file = file;
        lblFileTitle.setText("File " + file.getSortOrder());

    }

    public ScannedFile getFile() {
        return file;
    }
}