package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BE.UserProfile;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// ControlsFX imports
import org.controlsfx.control.CheckComboBox;

// java imports
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AssignEmployeeProfileController implements Initializable {

    @FXML private CheckComboBox<User> employeeDropdown;
    @FXML private ListView<User> assignedList;

    private VBox overlay;
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

            List<UserProfile> assigned = userModel.getEmployeesForProfile(currentProfileId);

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
                                userModel.assignEmployees(u.getId(), currentProfileId);
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
                                userModel.removeEmployees(u.getId(), currentProfileId);
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