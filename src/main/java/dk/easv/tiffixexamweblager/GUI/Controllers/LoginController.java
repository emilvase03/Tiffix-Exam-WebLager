package dk.easv.tiffixexamweblager.GUI.Controllers;

import dk.easv.tiffixexamweblager.BE.Role;
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    private static final PseudoClass DANGER = PseudoClass.getPseudoClass("danger");

    private final UserModel userModel = new UserModel();

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblUserError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblGeneralError;
    @FXML private Button btnLogin;

    @FXML
    private void initialize() {

        btnLogin.disableProperty().bind(userModel.loadingProperty());

        userModel.loginFailedProperty().addListener((obs, oldVal, isFailed) -> {
            if (isFailed) {
                showGeneralError("Invalid username or password.");
            }
        });

        userModel.loggedInUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {

                if (newUser.getRole() == Role.ADMIN) {
                    ViewHandler.ADMIN_DASHBOARD.show(true);
                } else {
                    //ViewHandler.COORD_DASHBOARD.show(true);
                }

                ViewHandler.LOGIN.close();
            }
        });

        txtUsername.textProperty().addListener((obs, o, n) ->
                clearFieldError(txtUsername, lblUserError));

        txtPassword.textProperty().addListener((obs, o, n) ->
                clearFieldError(txtPassword, lblPasswordError));
    }

    @FXML
    public void onLogin(ActionEvent event) {
        clearAllErrors();

        if (!validateInputs()) return;

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        userModel.loginUser(username, password);
    }

    private boolean validateInputs() {
        boolean valid = true;

        if (txtUsername.getText().isBlank()) {
            showFieldError(txtUsername, lblUserError, "Username is required");
            valid = false;
        }

        if (txtPassword.getText().isBlank()) {
            showFieldError(txtPassword, lblPasswordError, "Password is required");
            valid = false;
        }

        return valid;
    }

    private void showFieldError(TextField field, Label label, String message) {
        field.pseudoClassStateChanged(DANGER, true);
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearFieldError(TextField field, Label label) {
        field.pseudoClassStateChanged(DANGER, false);
        label.setVisible(false);
        label.setManaged(false);
    }

    private void showGeneralError(String message) {
        lblGeneralError.setText(message);
        lblGeneralError.setVisible(true);
        lblGeneralError.setManaged(true);
    }

    private void clearAllErrors() {
        clearFieldError(txtUsername, lblUserError);
        clearFieldError(txtPassword, lblPasswordError);

        lblGeneralError.setVisible(false);
        lblGeneralError.setManaged(false);
    }

}