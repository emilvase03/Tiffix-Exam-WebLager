package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.GUI.Controllers.ProfilesTabController;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileModel;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileRuleModel;
import dk.easv.tiffixexamweblager.GUI.Models.RuleModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    private Profile profileToBeUpdated;
    private RuleModel ruleModel;
    private ProfileRuleModel profileRuleModel;
    private ObservableList<Rule> allRules = FXCollections.observableArrayList();
    private ObservableList<Rule> profileRules = FXCollections.observableArrayList();

    public CreateProfileController() {
        try {
            profileModel = new ProfileModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate ProfileModel.");
        }
        try {
            ruleModel = new RuleModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate RuleModel");
        }
        try {
            profileRuleModel = new ProfileRuleModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate ProfileRuleModel");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListeners();
    }

    private void setupOverLay() {
        rulesDropdown.getItems().clear();
        rulesList.getItems().clear();
        profileRules.clear();
        allRules.clear();

        rulesDropdown.setTitle("Select Rules");

        try {
            allRules.addAll(ruleModel.getAllRules());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to retrieve every profile rule from database");
        }

        // If update vs If create
        if (updateProfile) {
            try {
                profileRules.addAll(ruleModel.getRulesForProfile(profileToBeUpdated));
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to retrieve profile rules from database");
            }
            rulesDropdown.getItems().addAll(allRules);
            for (Rule r : profileRules) {
                rulesDropdown.getCheckModel().check(r);
            }

        } else {

            rulesDropdown.getItems().addAll(allRules);
        }
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

        // If update vs If create
        if (updateProfile) {
            try {
                if (profileToBeUpdated != null) {
                    profileToBeUpdated.setTitle(txtTitle.getText().trim());
                    profileModel.updateProfile(profileToBeUpdated);

                    profileRuleModel.deleteRulesForProfile(profileToBeUpdated);
                    for (Rule r : rulesList.getItems())
                        profileRuleModel.addRuleToProfile(profileToBeUpdated, r);

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
                for (Rule r : rulesList.getItems())
                    profileRuleModel.addRuleToProfile(profile, r);

                txtTitle.clear();
                handleClose();
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to create new profile.");
            }
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

    public void preloadUpdateWindow(Profile profile) {
        txtTitle.setText(profile.getTitle());
        lblHeader.setText("Edit profile.");
        updateProfile = true;
        profileToBeUpdated = profile;
        setupOverLay();
    }

    public void preloadCreateWindow() {
        lblHeader.setText("Create a new profile.");
        updateProfile = false;
        setupOverLay();
    }

    private void handleClose() {
        if (overlay != null) {
            updateProfile = false;
            txtTitle.setStyle("");
            rulesList.setStyle("");
            overlay.setVisible(false);
            overlay.setManaged(false);
        }
    }

    private void setupListeners() {
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
}
