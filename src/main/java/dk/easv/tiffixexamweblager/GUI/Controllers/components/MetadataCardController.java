package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Metadata;
import dk.easv.tiffixexamweblager.GUI.Controllers.MetadataTabController;
import dk.easv.tiffixexamweblager.GUI.Models.BoxDocumentModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// Java imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class MetadataCardController {
    @FXML private TextArea txtArea;

    private VBox overlay;
    private BoxDocumentModel boxDocumentModel;
    private Box boxToBeUpdated;
    private MetadataTabController metadataTabController;

    public void init() {
        try {
            boxDocumentModel = new BoxDocumentModel();
        } catch (Exception e) {
            AlertHelper.showError("Error" , "Failed to initialize box model.");
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            boxDocumentModel.saveMetadata(
                    new Metadata(
                            boxToBeUpdated.getId(),
                            boxToBeUpdated.getDocumentsAmount(),
                            boxToBeUpdated.getFilesAmount(),
                            txtArea.getText().trim()));

            boxToBeUpdated.setNotes(txtArea.getText().trim());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to save metadata.");
        }

        hideOverlay();
        metadataTabController.getTable().refresh();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        hideOverlay();
    }

    public void preloadWindow(Box box) {
        boxToBeUpdated = box;
        txtArea.setText(box.getNotes());
    }

    public void setOverlay(VBox overlay) {
        this.overlay = overlay;
    }

    private void hideOverlay() {
        if (overlay != null) {
            overlay.setVisible(false);
            overlay.setManaged(false);
        }
        txtArea.clear();
    }

    public void setMetadataTabController(MetadataTabController controller) {
        this.metadataTabController = controller;
    }
}
