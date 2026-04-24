package dk.easv.tiffixexamweblager.GUI.Controllers;

import atlantafx.base.controls.ModalPane;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.NewEmployeeController;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;

public class EmployeesTabController {


    @FXML private ModalPane                 modalPane;
    @FXML private TableView<User>           tblEmployeeContainer;
    @FXML private TableColumn<User, String> colFirstName;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, Void>   colManage;

    private final UserModel userModel = new UserModel();

    @FXML
    public void initialize() {
        colFirstName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFirstName()));
        colLastName .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLastName()));
        colUsername .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        tblEmployeeContainer.setItems(userModel.getEmployees());
        loadEmployees();
    }

    private void loadEmployees() {
        try {
            userModel.loadCoordinators();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load employees.");
        }
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
