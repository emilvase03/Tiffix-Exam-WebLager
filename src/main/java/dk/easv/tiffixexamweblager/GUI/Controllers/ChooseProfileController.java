
package dk.easv.tiffixexamweblager.GUI.Controllers;

import atlantafx.base.controls.ModalPane;
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
public class ChooseProfileController {

    @FXML
    private Button btnSelectProfile;
    @FXML
    private VBox profileList;

    private ModalPane modalPane;
    private ProfileModel profileModel;

    private final List<Profile> selectedProfiles = new ArrayList<>();

    public void init(ModalPane modalPane) {
        this.modalPane = modalPane;

        try {
            profileModel = new ProfileModel();
            loadAssignedProfiles();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load profiles.");
        }
    }

    private void loadAssignedProfiles() throws Exception {
        User currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser == null) {
            return;
        }

        List<Profile> profiles =
                profileModel.getUserProfileManager()
                        .getProfilesForEmployee(currentUser.getId());

        profileList.getChildren().clear();

        for (Profile profile : profiles) {
            profileList.getChildren().add(createProfileRow(profile));
        }

        btnSelectProfile.setDisable(profiles.isEmpty());
    }

    private HBox createProfileRow(Profile profile) {
        CheckBox cb = new CheckBox(profile.getTitle());

        cb.selectedProperty().addListener((obs, oldVal, selected) -> {
            if (selected) {
                selectedProfiles.add(profile);
            } else {
                selectedProfiles.remove(profile);
            }
            btnSelectProfile.setDisable(selectedProfiles.isEmpty());
        });

        HBox row = new HBox(cb);
        row.getStyleClass().add("profile-item");
        return row;
    }
    @FXML
    private void onBtnClose() {
        modalPane.hide();
    }

    @FXML
    private void onBtnSelectProfile() {
        if (selectedProfiles.isEmpty()) return;

        UserSession.getInstance().setActiveProfiles(selectedProfiles);
        modalPane.hide();
    }
}