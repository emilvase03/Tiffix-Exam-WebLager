package dk.easv.tiffixexamweblager.GUI.Controllers.components;

import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.GUI.Controllers.BoxesTabController;
import dk.easv.tiffixexamweblager.GUI.Models.BoxModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;

public class BoxCardController {

    @FXML private Label     lblHeader;
    @FXML private TextField txtNumber;
    @FXML private TextField txtTitle;

    private VBox               overlay;
    private BoxesTabController boxesTabController;
    private BoxModel           boxModel;
    private int                loggedInUserId;
    private String             loggedInUsername;

    public void setOverlay(VBox overlay) {
        this.overlay = overlay;
    }

    public void setBoxesTabController(BoxesTabController controller) {
        this.boxesTabController = controller;
    }

    public void setBoxModel(BoxModel boxModel) {
        this.boxModel = boxModel;
    }

    public void setLoggedInUser(int userId, String username) {
        this.loggedInUserId   = userId;
        this.loggedInUsername = username;
    }

    public void preloadCreateWindow() {
        lblHeader.setText("Create a new box.");
        txtNumber.clear();
        txtTitle.clear();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String numberText = txtNumber.getText().trim();
        String title      = txtTitle.getText().trim();

        if (numberText.isEmpty() || title.isEmpty()) {
            AlertHelper.showError("Validation", "Box number and title are required.");
            return;
        }

        int number;
        try {
            number = Integer.parseInt(numberText);
        } catch (NumberFormatException e) {
            AlertHelper.showError("Validation", "Box number must be a whole number.");
            return;
        }

        Box newBox = new Box(0, number, title, LocalDateTime.now(),
                loggedInUserId, 0, 0);
        newBox.setCreatedByUsername(loggedInUsername);

        try {
            boxModel.createBox(newBox);
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to create box.");
            return;
        }

        hideOverlay();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        hideOverlay();
    }

    private void hideOverlay() {
        if (overlay != null) {
            overlay.setVisible(false);
            overlay.setManaged(false);
        }
        txtNumber.clear();
        txtTitle.clear();
    }
}