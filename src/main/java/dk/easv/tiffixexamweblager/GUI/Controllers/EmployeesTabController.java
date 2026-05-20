package dk.easv.tiffixexamweblager.GUI.Controllers;

import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import atlantafx.base.controls.ModalPane;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class EmployeesTabController {

    @FXML private ModalPane              modalPane;
    @FXML private TableView<User>        tblEmployeeContainer;
    @FXML private TableColumn<User, String>  colFirstName;
    @FXML private TableColumn<User, String>  colLastName;
    @FXML private TableColumn<User, String>  colUsername;
    @FXML private TableColumn<User, Void>    colManage;
    @FXML private TableColumn<User, Boolean> colActive;

    @FXML private VBox                            assignProfileOverlay;
    @FXML private AssignProfileToEmployeeController assignProfileToEmployeeController;

    private UserModel userModel;

    public EmployeesTabController() {
        try {
            userModel = new UserModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate UserModel");
        }
    }

    @FXML
    public void initialize() {
        colFirstName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFirstName()));
        colLastName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLastName()));
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        tblEmployeeContainer.setItems(userModel.getEmployees());
        setupActiveColumn();
        setupManageColumn();
        loadEmployees();

        assignProfileToEmployeeController.setOverlay(assignProfileOverlay);
    }

    private void loadEmployees() {
        try {
            userModel.loadEmployees();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load employees.");
        }
    }

    private void setupManageColumn() {
        colManage.setCellFactory(col -> new TableCell<>() {

            private final Button btnEdit   = new Button();
            private final Button btnAssign = new Button();
            private final HBox   container = new HBox(8, btnEdit, btnAssign);

            {
                btnEdit.setGraphic(new FontIcon("bi-pencil"));
                btnEdit.getStyleClass().add("icon-button");
                btnEdit.setOnAction(e ->
                        onBtnEditUser(getTableView().getItems().get(getIndex())));

                btnAssign.setGraphic(new FontIcon("bi-person-badge"));
                btnAssign.getStyleClass().add("icon-button");
                btnAssign.setOnAction(e ->
                        onBtnAssignProfiles(getTableView().getItems().get(getIndex())));

                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void setupActiveColumn() {
        colActive.setCellValueFactory(data ->
                new SimpleBooleanProperty(data.getValue().getIsDeleted()));

        colActive.setCellFactory(col -> new TableCell<>() {
            final Button btnActive     = new Button();
            final Button btnDeactivate = new Button();
            final HBox   container     = new HBox();

            {
                btnActive.setGraphic(new FontIcon("bi-check-square"));
                btnActive.getStyleClass().addAll("icon-button");
                btnActive.setOnAction(e -> handleToggle(true));

                btnDeactivate.setGraphic(new FontIcon("bi-dash-square"));
                btnDeactivate.getStyleClass().addAll("icon-button", "danger");
                btnDeactivate.setOnAction(e -> handleToggle(false));

                container.setAlignment(Pos.CENTER);
            }

            private void handleToggle(boolean newDeletedState) {
                User user = getTableView().getItems().get(getIndex());
                try {
                    userModel.toggleSoftDelete(user, success -> {
                        if (success) {
                            user.setIsDeleted(newDeletedState);
                            getTableView().refresh();
                        }
                    });
                } catch (Exception ex) {
                    AlertHelper.showError("Error", "Failed to toggle active status of " + user.getUsername());
                }
            }

            @Override
            protected void updateItem(Boolean isDeleted, boolean empty) {
                super.updateItem(isDeleted, empty);
                if (empty || isDeleted == null) { setGraphic(null); return; }
                container.getChildren().setAll(isDeleted ? btnDeactivate : btnActive);
                setGraphic(container);
            }
        });
    }

    private void onBtnAssignProfiles(User user) {
        if (user == null) return;
        assignProfileToEmployeeController.preload(user);
        assignProfileOverlay.setVisible(true);
        assignProfileOverlay.setManaged(true);
    }

    private void onBtnEditUser(User user) {
        if (user == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/EditEmployeeView.fxml"));
            Parent content = loader.load();
            EditEmployeeController controller = loader.getController();
            controller.init(userModel, user, modalPane);
            modalPane.show(content);
        } catch (IOException e) {
            AlertHelper.showError("Error", "Failed to open Edit Employee form.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleCreate() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/NewEmployeeView.fxml"));
            Parent content = loader.load();
            NewEmployeeController controller = loader.getController();
            controller.init(userModel, modalPane, userModel.getEmployees());
            modalPane.show(content);
        } catch (IOException e) {
            AlertHelper.showError("Error", "Failed to open New Employee form.");
            e.printStackTrace();
        }
    }
}