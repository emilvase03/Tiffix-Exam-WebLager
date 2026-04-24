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
    private boolean updateProfile = false;
    private Profile profileToBeUpdated;

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

        if (updateProfile) {
            try {
                if (profileToBeUpdated != null) {
                    profileToBeUpdated.setTitle(txtTitle.getText().trim());
                    profileModel.updateProfile(profileToBeUpdated);
                    profilesTabController.getTable().refresh();
                    txtTitle.clear();
                    handleClose();
                    updateProfile = false;
                }
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to update profile.");
            }
        } else {
            try {
                Profile profile = new Profile(txtTitle.getText().trim());
                profileModel.createProfile(profile);
                profilesTabController.getTable().getItems().add(profile);
                txtTitle.clear();
                handleClose();
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to create new profile.");
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        txtTitle.clear();
        updateProfile = false;
        handleClose();
    }

    public void setOverlay(VBox overlay) {
        this.overlay = overlay;
    }

    public void setProfilesTabController(ProfilesTabController profilesTabController) {
        this.profilesTabController = profilesTabController;
    }

    public void preloadWindow(Profile profile) {
        txtTitle.setText(profile.getTitle());
        updateProfile = true;
        profileToBeUpdated = profile;
    }

    private void handleClose() {
        if (overlay != null) {
            overlay.setVisible(false);
            overlay.setManaged(false);
        }
    }
}
