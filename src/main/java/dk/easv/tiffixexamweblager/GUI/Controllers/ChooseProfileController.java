package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Models.DocumentModel;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// AtlantaFX imports
import atlantafx.base.controls.ModalPane;

// Java imports
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChooseProfileController {

    @FXML private Button btnSelectProfile;
    @FXML private VBox profileList;
    @FXML private TextField txtfieldSearchbar;

    private ModalPane modalPane;
    private ProfileModel profileModel;
    private DocumentModel documentModel;
    private List<Profile> profiles = new ArrayList<>();

    @FXML private ComboBox<Box> boxComboBox;

    private final List<Profile> selectedProfiles = new ArrayList<>();

    private Runnable onSessionReady;

    public void init(ModalPane modalPane, Runnable onSessionReady) {
        this.modalPane = modalPane;
        this.onSessionReady = onSessionReady;

        try {
            profileModel = new ProfileModel();
            documentModel = new DocumentModel();
            loadAssignedProfiles();
            loadBoxes();
            setupSearchbar();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load profiles or boxes.");
        }
    }

    private void loadAssignedProfiles() throws Exception {
        User currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser == null) {
            return;
        }

        profiles = profileModel.getUserProfileManager().getProfilesForEmployee(currentUser.getId());

        profileList.getChildren().clear();

        for (Profile profile : profiles) {
            profileList.getChildren().add(createProfileRow(profile));
        }
    }

    private void loadBoxes() throws Exception {
        List<Box> boxes = documentModel.getAllBoxes();
        boxComboBox.getItems().setAll(boxes);

        if (!boxes.isEmpty())
            boxComboBox.getSelectionModel().selectFirst();
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
}