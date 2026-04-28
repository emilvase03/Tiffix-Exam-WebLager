package dk.easv.tiffixexamweblager.GUI.Controllers;

import atlantafx.base.theme.Styles;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BE.UserProfile;
import dk.easv.tiffixexamweblager.BLL.UserProfileManager;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

public class AssignEmployeeProfileController {

    @FXML private ComboBox<User> employeeDropdown;
    @FXML private ListView<User> assignedList;

    private UserProfileManager userProfileManager;
    private UserModel userModel;
    private int currentProfileId;
    private List<User> allEmployees;

    public void init(UserModel userModel, int profileId) {
        this.userModel        = userModel;
        this.currentProfileId = profileId;

        try {
            userProfileManager = new UserProfileManager();
            allEmployees       = userModel.getEmployees();
        } catch (Exception e) {
            AlertHelper.showError("Failed to initialize", e.getMessage());
            return;
        }

        setupAssignedListCellFactory();

        if (!allEmployees.isEmpty()) {
            loadAssignedEmployees();
        } else {
            userModel.loadEmployees(this::loadAssignedEmployees);
        }
    }

    private void setupAssignedListCellFactory() {
        assignedList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Button btnRemove = new Button("✕");
                    btnRemove.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER, Styles.SMALL);
                    btnRemove.setOnAction(e -> handleRemove(item));

                    HBox row = new HBox(8, btnRemove, new Label(item.getFirstName() + " " + item.getLastName()));
                    row.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(row);
                    setText(null);
                }
            }
        });
    }

    private void loadAssignedEmployees() {
        try {
            List<UserProfile> assigned = userProfileManager.getCoordinatorsForEvent(currentProfileId);
            ObservableList<User> assignedUsers = FXCollections.observableArrayList();

            for (UserProfile up : assigned) {
                allEmployees.stream()
                        .filter(u -> u.getId() == up.getUserId())
                        .findFirst()
                        .ifPresent(assignedUsers::add);
            }

            assignedList.setItems(assignedUsers);

            ObservableList<User> available = allEmployees.stream()
                    .filter(u -> assignedUsers.stream().noneMatch(a -> a.getId() == u.getId()))
                    .collect(FXCollections::observableArrayList, ObservableList::add, ObservableList::addAll);

            employeeDropdown.setItems(available);

        } catch (Exception e) {
            AlertHelper.showError("Failed to load employees", e.getMessage());
        }
    }

    @FXML
    private void handleAssign(ActionEvent actionEvent) {
        User selected = employeeDropdown.getValue();

        if (selected == null) {
            AlertHelper.showError("No selection", "Please select an employee to assign.");
            return;
        }

        try {
            userProfileManager.assignCoordinator(selected.getId(), currentProfileId);
            assignedList.getItems().add(selected);
            employeeDropdown.getItems().remove(selected);
            employeeDropdown.setValue(null);
        } catch (Exception e) {
            AlertHelper.showError("Failed to assign employee", e.getMessage());
        }
    }

    private void handleRemove(User user) {
        if (user == null)
            return;

        try {
            userProfileManager.removeCoordinator(user.getId(), currentProfileId);
            assignedList.getItems().remove(user);
            employeeDropdown.getItems().add(user);
        } catch (Exception e) {
            AlertHelper.showError("Failed to remove employee", e.getMessage());
        }
    }

    @FXML
    private void handleClose(ActionEvent actionEvent) {
        ViewHandler.ASSIGN_EMPLOYEE_PROFILE.close();
        ViewHandler.ASSIGN_EMPLOYEE_PROFILE.reset();
    }
}