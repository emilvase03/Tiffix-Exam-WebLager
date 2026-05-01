package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.GUI.Controllers.ProfilesTabController;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// Java imports
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateProfileController implements Initializable {
    @FXML private TextField txtTitle;
    @FXML private Label lblHeader;
    @FXML private CheckComboBox<Rule> rulesDropdown;
    @FXML private ListView<Rule> rulesList;

    private VBox overlay;
    private ProfileModel profileModel;
    private ProfilesTabController profilesTabController;
    private boolean updateProfile = false;
    private boolean rulesIsUpdating = false;
    private Profile profileToBeUpdated;

    public CreateProfileController() {
        try {
            profileModel = new ProfileModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate ProfileModel.");
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Rule r1 = new Rule(1,"Brightness", 5);
        rulesDropdown.getItems().add(r1);
        Rule r2 = new Rule(2, "Rotate 90", 90);
        rulesDropdown.getItems().add(r2);
        Rule r3 = new Rule(3, "Rotate 180", 180);
        rulesDropdown.getItems().add(r3);

        setupOverLay();

    }

    private void setupOverLay() {
        rulesDropdown.setTitle("Select Rules");
        rulesDropdown.getCheckModel().getCheckedItems().addListener((ListChangeListener<Rule>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    rulesList.getItems().addAll(change.getAddedSubList());
                }
                if (change.wasRemoved()) {
                    rulesList.getItems().removeAll(change.getRemoved());
                }
            }
        } );
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (txtTitle.getText().isBlank()) {
            txtTitle.setStyle("-fx-border-color: #FF3D32; -fx-border-width: 1;");
            return;
        }
        if (rulesList.getItems().isEmpty()) {
            rulesList.setStyle("-fx-border-color: #FF3D32;");
            return;
        }

        if (updateProfile) {
            try {
                if (profileToBeUpdated != null) {
                    profileToBeUpdated.setTitle(txtTitle.getText().trim());
                    profileModel.updateProfile(profileToBeUpdated);
                    profilesTabController.getTable().refresh();
                    txtTitle.clear();
                    lblHeader.setText("Create a new profile.");
                    handleClose();
                    updateProfile = false;
                }
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to update profile.");
            }
        } else {
            try {
                Profile profile = profileModel.createProfile(new Profile(txtTitle.getText().trim()));
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
        lblHeader.setText("Create a new profile.");
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
        lblHeader.setText("Edit profile.");
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
