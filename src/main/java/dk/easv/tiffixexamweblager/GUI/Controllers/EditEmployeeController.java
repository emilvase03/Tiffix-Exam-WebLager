package dk.easv.tiffixexamweblager.GUI.Controllers;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import dk.easv.tiffixexamweblager.BE.Role;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditEmployeeController {
    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtUserName;
    @FXML
    private TextField txtPassword;
    @FXML
    private UserModel userModel;
    @FXML
    private User userToEdit;
   
    private ModalPane modalPane;
    @FXML
    private Label lblFirstNameError;
    @FXML
    private Label lblLastNameError;
    @FXML
    private Label lblUsernameError;
    @FXML
    private Label lblPasswordError;

    public void init(UserModel userModel, User user,ModalPane modalPane) {
        this.userModel  = userModel;
        this.userToEdit = user;
        this.modalPane    = modalPane;
        clearAllErrors();


        txtFirstName.setText(user.getFirstName());
        txtLastName.setText(user.getLastName());
        txtUserName.setText(user.getUsername());
        txtPassword.setText("");

    }

    @FXML
    private void onBtnSave(ActionEvent actionEvent) {

        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String username = txtUserName.getText().trim();
        String password = txtPassword.getText().trim();

        clearAllErrors();
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

        if (!valid) return;

        userToEdit.setFirstName(firstName);
        userToEdit.setLastName(lastName);
        userToEdit.setUsername(username);
        userToEdit.setRole(Role.EMPLOYEE);

        String rawPassword = password.isBlank() ? null : password;

        try {
            userModel.updateUser(userToEdit, rawPassword);
            modalPane.hide();
        } catch (Exception ex) {
            AlertHelper.showError("Edit Employee failed", ex.getMessage());
        }
    }
    @FXML
    private void onBtnCancel(ActionEvent actionEvent) {
        if (hasUnsavedInput()) {
            boolean confirm = AlertHelper.showConfirmation(
                    "Cancel changes",
                    "Unsaved changes will be lost."
            );

            if (!confirm) return;
        }

        modalPane.hide();
    }
    private void clearAllErrors() {
        for (TextField f : new TextField[]{ txtFirstName, txtLastName, txtUserName, txtPassword })
            f.getStyleClass().remove(Styles.DANGER);
        for (Label l : new Label[]{ lblFirstNameError, lblLastNameError, lblUsernameError, lblPasswordError }) {
            l.setText("");
            l.setVisible(false);

        }
    }
    private void markError(TextField field, Label label, String message) {
        if (!field.getStyleClass().contains(Styles.DANGER))
            field.getStyleClass().add(Styles.DANGER);
        label.setText(message);
        label.setVisible(true);
        label.setOpacity(1);
    }
    private boolean hasUnsavedInput() {
        return !txtFirstName.getText().isBlank() || !txtLastName.getText().isBlank()
                || !txtUserName.getText().isBlank()  ;
    }
}
