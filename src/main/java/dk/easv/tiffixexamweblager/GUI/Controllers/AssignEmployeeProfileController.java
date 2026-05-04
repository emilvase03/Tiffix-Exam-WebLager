package dk.easv.tiffixexamweblager.GUI.Controllers;

import atlantafx.base.theme.Styles;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BE.UserProfile;
import dk.easv.tiffixexamweblager.BLL.UserProfileManager;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AssignEmployeeProfileController implements Initializable {

    @FXML private CheckComboBox<User> employeeDropdown;
    @FXML private ListView<User> assignedList;

    private VBox overlay;
    private UserProfileManager userProfileManager;
    private UserModel userModel;
    private int currentProfileId;
    private boolean isLoading = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListener();
    }

    public void setOverlay(VBox overlay) {
        this.overlay = overlay;
    }

    public void preload(UserModel userModel, int profileId) {
        this.userModel        = userModel;
        this.currentProfileId = profileId;

        employeeDropdown.getItems().clear();
        employeeDropdown.getCheckModel().clearChecks();
        assignedList.getItems().clear();
        employeeDropdown.setTitle("Select Employees");

        try {
            userProfileManager = new UserProfileManager();
        } catch (Exception e) {
            AlertHelper.showError("Failed to initialize", e.getMessage());
            return;
        }

        List<User> employees = userModel.getEmployees();
        if (!employees.isEmpty()) {
            populateDropdown(employees);
        } else {
            userModel.loadEmployees(() -> populateDropdown(userModel.getEmployees()));
        }
    }

    private void populateDropdown(List<User> allEmployees) {
        try {
            employeeDropdown.getItems().addAll(allEmployees);

            List<UserProfile> assigned = userProfileManager.getCoordinatorsForEvent(currentProfileId);

            isLoading = true;
            for (User u : allEmployees) {
                boolean isAssigned = assigned.stream().anyMatch(up -> up.getUserId() == u.getId());
                if (isAssigned) {
                    employeeDropdown.getCheckModel().check(u);
                }
            }
            isLoading = false;
        } catch (Exception e) {
            isLoading = false;
            AlertHelper.showError("Failed to load employees", e.getMessage());
        }
    }

    private void setupListener() {
        employeeDropdown.getCheckModel().getCheckedItems().addListener((ListChangeListener<User>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (User u : change.getAddedSubList()) {
                        assignedList.getItems().add(u);
                        if (!isLoading) {
                            try {
                                userProfileManager.assignCoordinator(u.getId(), currentProfileId);
                            } catch (Exception e) {
                                AlertHelper.showError("Failed to assign employee", e.getMessage());
                            }
                        }
                    }
                }
                if (change.wasRemoved()) {
                    for (User u : change.getRemoved()) {
                        assignedList.getItems().remove(u);
                        if (!isLoading) {
                            try {
                                userProfileManager.removeCoordinator(u.getId(), currentProfileId);
                            } catch (Exception e) {
                                AlertHelper.showError("Failed to remove employee", e.getMessage());
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