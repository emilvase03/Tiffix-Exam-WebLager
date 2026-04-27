package dk.easv.tiffixexamweblager.GUI.Controllers;

import atlantafx.base.controls.ModalPane;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.NewEmployeeController;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class  EmployeesTabController {


    @FXML
    private ModalPane modalPane;
    @FXML
    private TableView<User> tblEmployeeContainer;
    @FXML
    private TableColumn<User, String> colFirstName;
    @FXML
    private TableColumn<User, String> colLastName;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, Void> colManage;

    private final UserModel userModel = new UserModel();

    @FXML
    public void initialize() {
        colFirstName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFirstName()));
        colLastName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLastName()));
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        tblEmployeeContainer.setItems(userModel.getEmployees());
        setupManageColumn();
        loadEmployees();
    }

    private void loadEmployees() {
        try {
            userModel.loadCoordinators();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load employees.");
        }
    }

    private void setupManageColumn() {

        colManage.setCellFactory(col -> new TableCell<>() {

            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();

            private final HBox container = new HBox(8, btnEdit, btnDelete);

            {
                FontIcon editIcon = new FontIcon("bi-pencil");
                FontIcon deleteIcon = new FontIcon("bi-trash");

                btnEdit.setGraphic(editIcon);
                btnDelete.setGraphic(deleteIcon);

                btnEdit.getStyleClass().add("icon-button");
                btnDelete.getStyleClass().add("icon-button");
                btnDelete.getStyleClass().add("danger");

                btnEdit.setOnAction(e ->
                        onBtnEditUser(getTableView().getItems().get(getIndex()))
                );

                btnDelete.setOnAction(e ->
                        onBtnDeleteUser(getTableView().getItems().get(getIndex()))
                );

                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void onBtnDeleteUser(User user) {

        if (user == null) return;

        boolean confirmed = AlertHelper.showConfirmation(
                "Delete User",
                "Are you sure you want to delete " + user.getUsername() + "?"
        );

        if (confirmed) {
            try {
                userModel.deleteUser(user);
                userModel.getEmployees().remove(user);
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to delete user.");
                e.printStackTrace();
            }
        }
    }

    private void onBtnEditUser(User user) {
    }

    @FXML
    private void onBtnCreateEmployee() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/NewEmployeeView.fxml")
            );

            Parent content = loader.load();

            NewEmployeeController controller = loader.getController();
            controller.init(
                    userModel,
                    modalPane,
                    userModel.getEmployees()
            );

            modalPane.show(content);

        } catch (IOException e) {
            AlertHelper.showError("Error", "Failed to open New Employee form.");
            e.printStackTrace();
        }
    }
}

