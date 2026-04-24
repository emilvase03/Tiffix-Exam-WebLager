package dk.easv.tiffixexamweblager.GUI.Controllers.components;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import dk.easv.tiffixexamweblager.BE.Role;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;


public class NewEmployeeController {

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtUserName;
    @FXML private TextField txtPassword;



    private UserModel            userModel;
    private ModalPane            modalPane;
    @FXML
    private Label lblFirstNameError;
    @FXML
    private Label lblLastNameError;
    @FXML
    private Label lblUsernameError;
    @FXML
    private Label lblPasswordError;


    public void init(UserModel userModel, ModalPane modalPane, ObservableList<User> employeeList) {
        this.userModel    = userModel;
        this.modalPane    = modalPane;
        clearAllErrors();
    }

    @FXML
    private void onBtnCreate() {
        clearAllErrors();

        String firstName = txtFirstName.getText().trim();
        String lastName  = txtLastName.getText().trim();
        String username  = txtUserName.getText().trim();
        String password  = txtPassword.getText().trim();

        boolean valid = true;

        if (firstName.isBlank()) {
            markError(txtFirstName, lblFirstNameError, "First name is required");
            valid = false;
        }
        if (lastName.isBlank()) {
            markError(txtLastName, lblLastNameError, "Last name is required");
            valid = false;
        }
        if (username.isBlank()) {
            markError(txtUserName, lblUsernameError, "Username is required");
            valid = false;
        }
        if (password.isBlank()) {
            markError(txtPassword, lblPasswordError, "Password is required");
            valid = false;
        }

        if (!valid) return;

        try {

            userModel.createUser(firstName, lastName, username, password, Role.EMPLOYEE);
            modalPane.hide();

        } catch (Exception ex) {
            AlertHelper.showError("Create employee failed", ex.getMessage());
        }
    }


    @FXML
    private void onBtnCancel() {

        if (hasUnsavedInput()) {
            boolean confirm = AlertHelper.showConfirmation(
                    "Cancel changes",
                    "Entered information will be lost."
            );

            if (!confirm) return;
        }

        modalPane.hide();
    }

    private void markError(TextField field, Label label, String message) {
        if (!field.getStyleClass().contains(Styles.DANGER))
            field.getStyleClass().add(Styles.DANGER);
        label.setText(message);
        label.setVisible(true);
        label.setOpacity(1);
    }

    private void clearAllErrors() {
        for (TextField f : new TextField[]{ txtFirstName, txtLastName, txtUserName, txtPassword })
            f.getStyleClass().remove(Styles.DANGER);
        for (Label l : new Label[]{ lblFirstNameError, lblLastNameError, lblUsernameError, lblPasswordError }) {
            l.setText("");
            l.setVisible(false);

        }
    }

    private boolean hasUnsavedInput() {
        return !txtFirstName.getText().isBlank() || !txtLastName.getText().isBlank()
                || !txtUserName.getText().isBlank()  || !txtPassword.getText().isBlank();
    }
}
