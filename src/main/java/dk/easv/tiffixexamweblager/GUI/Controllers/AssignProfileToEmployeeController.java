package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileRuleModel;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// ControlsFX imports
import org.controlsfx.control.CheckComboBox;

// Java imports
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AssignProfileToEmployeeController implements Initializable {
    @FXML private CheckComboBox<Profile> profileDropdown;
    @FXML private ListView<Profile>      assignedList;
    @FXML private Label                  lblHeader;

    private VBox               overlay;
    private UserModel userModel;
    private ProfileRuleModel profileRuleModel;
    private int                currentUserId;
    private boolean            isLoading = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListener();
    }

    public void setOverlay(VBox overlay) {
        this.overlay = overlay;
    }

    public void preload(User user) {
        this.currentUserId = user.getId();

        profileDropdown.getItems().clear();
        profileDropdown.getCheckModel().clearChecks();
        assignedList.getItems().clear();
        profileDropdown.setTitle("Select Profiles");
        lblHeader.setText("Assign Profiles — " + user.getFirstName() + " " + user.getLastName());

        try {
            userModel = new UserModel();
            profileRuleModel = new ProfileRuleModel();
        } catch (Exception e) {
            AlertHelper.showError("Failed to initialize", e.getMessage());
            return;
        }

        try {
            populateDropdown(profileRuleModel.getAllProfiles());
        } catch (Exception e) {
            AlertHelper.showError("Failed to load profiles", e.getMessage());
        }
    }

    private void populateDropdown(List<Profile> allProfiles) {
        try {
            profileDropdown.getItems().addAll(allProfiles);

            List<Profile> assigned = userModel.getProfilesForEmployee(currentUserId);

            isLoading = true;
            for (Profile p : allProfiles) {
                boolean isAssigned = assigned.stream()
                        .anyMatch(ap -> ap.getId() == p.getId());
                if (isAssigned) {
                    profileDropdown.getCheckModel().check(p);
                }
            }
            isLoading = false;

        } catch (Exception e) {
            isLoading = false;
            AlertHelper.showError("Failed to load assigned profiles", e.getMessage());
        }
    }

    private void setupListener() {
        profileDropdown.getCheckModel().getCheckedItems().addListener((ListChangeListener<Profile>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Profile p : change.getAddedSubList()) {
                        assignedList.getItems().add(p);
                        if (!isLoading) {
                            try {
                                userModel.assignEmployees(currentUserId, p.getId());
                            } catch (Exception e) {
                                AlertHelper.showError("Failed to assign profile", e.getMessage());
                            }
                        }
                    }
                }
                if (change.wasRemoved()) {
                    for (Profile p : change.getRemoved()) {
                        assignedList.getItems().remove(p);
                        if (!isLoading) {
                            try {
                                userModel.removeEmployees(currentUserId, p.getId());
                            } catch (Exception e) {
                                AlertHelper.showError("Failed to remove profile", e.getMessage());
                            }
                        }
                    }
                }
            }
        });
    }

    @FXML
    private void handleClose(ActionEvent actionEvent) {
        if (overlay != null) {
            overlay.setVisible(false);
            overlay.setManaged(false);
        }
    }
}