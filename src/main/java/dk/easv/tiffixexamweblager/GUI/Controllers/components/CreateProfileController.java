package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.GUI.Controllers.ProfilesTabController;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// Java imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class CreateProfileController {
    @FXML private TextField txtTitle;
    private VBox overlay;
    private ProfileModel profileModel;
    private ProfilesTabController profilesTabController;

    public CreateProfileController() {
        try {
            profileModel = new ProfileModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate ProfileModel.");
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (txtTitle.getText().isBlank()) {
            txtTitle.setStyle("-fx-border-color: #FF3D32; -fx-border-width: 1;");
            return;
        }

        Profile newProfile = new Profile(txtTitle.getText().trim());
        try {
            profileModel.createProfile(newProfile);
            handleClose();
            profilesTabController.getTable().getItems().add(newProfile);
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to create new profile.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        handleClose();
    }

    public void setOverlay(VBox overlay) {
        this.overlay = overlay;
    }

    public void setProfilesTabController(ProfilesTabController profilesTabController) {
        this.profilesTabController = profilesTabController;
    }

    private void handleClose() {
        if (overlay != null) {
            overlay.setVisible(false);
            overlay.setManaged(false);
        }
    }
}
