package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Models.BoxDocumentModel;
import dk.easv.tiffixexamweblager.GUI.Models.CustomerProfileModel;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileRuleModel;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// AtlantaFX imports
import atlantafx.base.controls.ModalPane;

// Java imports
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class ChooseScanSettingsController {
    @FXML private Button btnSelectProfile;
    @FXML private VBox profileList;
    @FXML private VBox vboxSelectToggle;
    @FXML private VBox vboxCreateToggle;
    @FXML private ToggleButton toggleSelect;
    @FXML private ToggleButton toggleCreate;
    @FXML private TextField txtfieldSearchbar;
    @FXML private TextField txtfieldTitle;
    @FXML private TextField txtfieldNumber;
    @FXML private ComboBox<Box> boxComboBox;
    @FXML private ComboBox<Customer> customerCombobox;

    private ModalPane modalPane;
    private ProfileRuleModel profileRuleModel;
    private BoxDocumentModel boxDocumentModel;
    private CustomerProfileModel customerProfileModel;
    private UserModel userModel;
    private List<Profile> profiles = new ArrayList<>();
    private final List<Profile> selectedProfiles = new ArrayList<>();
    private Runnable onSessionReady;

    public void init(ModalPane modalPane, Runnable onSessionReady) {
        this.modalPane = modalPane;
        this.onSessionReady = onSessionReady;
        setupSearchbar();
        setupBoxListener();
        setupBoxConverter();
        bindToggle();

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
            customerProfileModel = new CustomerProfileModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate CustomerProfileModel.");
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
        loadCustomers();
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

    private void loadCustomers() {
        try {
            customerCombobox.getItems().setAll(customerProfileModel.getAllCustomers());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load customers.");
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
                for (Node node : profileList.getChildren()) {
                    HBox hbox = (HBox) node;
                    CheckBox other = (CheckBox) hbox.getChildren().getFirst();
                    if (other != cb) {
                        other.setSelected(false);
                    }
                }
                selectedProfiles.clear();
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
        Box selectedBox;
        if (selectedProfiles.isEmpty())
            return;

        if (toggleCreate.isSelected()) {
            if (txtfieldTitle.getText().isBlank())
                return;
            if(txtfieldNumber.getText().isBlank())
                return;

            Customer customer = customerCombobox.getSelectionModel().getSelectedItem();

            if (customer == null)
                return;

            Box newBox = new Box(
                    -1,
                    Integer.parseInt(txtfieldNumber.getText().trim()),
                    txtfieldTitle.getText().trim(),
                    LocalDateTime.now(),
                    UserSession.getInstance().getCurrentUser().getId(),
                    0,
                    0,
                    customer.getId());

            newBox.setProfileId(selectedProfiles.getFirst().getId());
            try {
                selectedBox = boxDocumentModel.createBox(newBox);
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to create " + newBox.getTitle());
                return;
            }
        } else {
            selectedBox = boxComboBox.getValue();
        }

        if (selectedBox == null)
            return;

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

    private void setupBoxConverter() {
        boxComboBox.setConverter(new StringConverter<Box>() {
            @Override
            public String toString(Box box) {
                return box == null ? "" : box.getTitle();
            }

            @Override
            public Box fromString(String text) {
                if (text == null || text.trim().isEmpty())
                    return null;

                return boxComboBox.getItems().stream()
                        .filter(b -> b.getTitle().equalsIgnoreCase(text.trim()))
                        .findFirst()
                        .orElse(new Box(-1, -1, text.trim(), null, null, -1, -1, -1));
            }
        });

    }

    private void bindToggle() {
        vboxCreateToggle.visibleProperty().bind(toggleCreate.selectedProperty());
        vboxCreateToggle.managedProperty().bind(toggleCreate.selectedProperty());

        vboxSelectToggle.visibleProperty().bind(toggleSelect.selectedProperty());
        vboxSelectToggle.managedProperty().bind(toggleSelect.selectedProperty());
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