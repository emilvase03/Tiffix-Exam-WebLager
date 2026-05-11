package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Models.BoxDocumentModel;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileRuleModel;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// AtlantaFX imports
import atlantafx.base.controls.ModalPane;

// Java imports
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChooseScanSettingsController {
    @FXML private Button btnSelectProfile;
    @FXML private VBox profileList;
    @FXML private TextField txtfieldSearchbar;
    @FXML private ComboBox<Box> boxComboBox;

    private ModalPane modalPane;
    private ProfileRuleModel profileRuleModel;
    private BoxDocumentModel boxDocumentModel;
    private UserModel userModel;
    private List<Profile> profiles = new ArrayList<>();
    private final List<Profile> selectedProfiles = new ArrayList<>();
    private Runnable onSessionReady;

    public void init(ModalPane modalPane, Runnable onSessionReady) {
        this.modalPane = modalPane;
        this.onSessionReady = onSessionReady;
        setupSearchbar();
        setupBoxListener();

        try {
            profileRuleModel = new ProfileRuleModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate ProfileRuleModel.");
        }
        try {
            boxDocumentModel = new BoxDocumentModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate BoxDocumentModel.");
        }
        try {
            userModel = new UserModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate UserModel.");
        }
        try {
            loadAssignedProfiles();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load profiles.");
        }
        try {
            loadBoxes();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load boxes.");
        }
    }

    private void loadAssignedProfiles() throws Exception {
        User currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser == null) {
            return;
        }

        profiles = userModel.getUserProfileManager().getProfilesForEmployee(currentUser.getId());

        profileList.getChildren().clear();

        for (Profile profile : profiles) {
            profileList.getChildren().add(createProfileRow(profile));
        }
    }

    private void loadBoxes() throws Exception {
        List<Box> boxes = boxDocumentModel.getAllBoxes();
        boxComboBox.getItems().setAll(boxes);
    }

    private HBox createProfileRow(Profile profile) {
        CheckBox cb = new CheckBox(profile.getTitle());

        cb.selectedProperty().addListener((obs, oldVal, selected) -> {
            if (selected) {
                selectedProfiles.add(profile);
            } else {
                selectedProfiles.remove(profile);
            }
        });

        HBox row = new HBox(cb);
        row.getStyleClass().add("profile-item");
        row.setUserData(profile);
        return row;
    }

    @FXML
    private void onBtnClose() {
        modalPane.hide();
    }

    @FXML
    private void onBtnStartSession(ActionEvent actionEvent) {
        Box selectedBox = boxComboBox.getValue();
        if (selectedProfiles.isEmpty() || selectedBox == null) return;

        // store choices in the session
        UserSession.getInstance().setActiveProfiles(selectedProfiles);
        UserSession.getInstance().setActiveBox(selectedBox);

        modalPane.hide();

        // tells the dashboard to load documents
        if (onSessionReady != null)
            onSessionReady.run();
    }

    private void setupSearchbar() {
        txtfieldSearchbar.textProperty().addListener((obs, oldVal, newVal) -> {
            profileList.getChildren().clear();

            profiles.stream()
                    .filter(profile -> {
                        if (newVal == null || newVal.isBlank())
                            return true;
                        return profile.getTitle().toLowerCase().contains(newVal.toLowerCase());
                    })
                    .map(profile -> createProfileRow(profile))
                    .forEach(node -> profileList.getChildren().add(node));
        });
    }

    private void setupBoxListener() {
        // Re-apply lock whenever the profile list is updated via search
        profileList.getChildren().addListener((ListChangeListener<Node>) change -> {
            Box selectedBox = boxComboBox.getSelectionModel().getSelectedItem();
            if (selectedBox == null)
                return;

            applyLock(selectedBox);
        });

        // Apply lock when a different box is selected
        boxComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, selectedBox) -> {
            if (selectedBox == null)
                return;

            applyLock(selectedBox);
        });
    }

    private void applyLock(Box selectedBox) {
        for (Node node : profileList.getChildren()) {
            HBox hbox = (HBox) node;
            CheckBox cb = (CheckBox) hbox.getChildren().getFirst();
            Profile profile = (Profile) hbox.getUserData();

            if (selectedBox.getProfileId() != null) {
                if (profile.getId() == selectedBox.getProfileId()) {
                    selectedProfiles.remove(profile); // prevent listener firing duplicate
                    cb.setSelected(true);
                } else {
                    cb.setSelected(false);
                }
                cb.setDisable(true);
            } else {
                cb.setSelected(false);
                cb.setDisable(false);
            }
        }
    }
}