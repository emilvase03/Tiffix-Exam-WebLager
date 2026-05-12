package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.GUI.Controllers.BoxesTabController;
import dk.easv.tiffixexamweblager.GUI.Models.BoxDocumentModel;
import dk.easv.tiffixexamweblager.GUI.Models.CustomerProfileModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// Java imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;

public class BoxCardController {

    @FXML private Label lblHeader;
    @FXML private TextField txtNumber;
    @FXML private TextField txtTitle;
    @FXML private ComboBox<Customer> customerCombobox;

    private VBox overlay;
    private BoxesTabController boxesTabController;
    private BoxDocumentModel boxDocumentModel;
    private int loggedInUserId;
    private String loggedInUsername;

    private boolean updateBox = false;
    private Box boxToBeUpdated;
    private CustomerProfileModel customerProfileModel;

    public void setOverlay(VBox overlay) {
        this.overlay = overlay;
    }

    public void setBoxesTabController(BoxesTabController controller) {
        this.boxesTabController = controller;
    }

    public void setBoxModel(BoxDocumentModel boxDocumentModel) {
        this.boxDocumentModel = boxDocumentModel;
    }

    public void setLoggedInUser(int userId, String username) {
        this.loggedInUserId   = userId;
        this.loggedInUsername = username;
    }

    public void preloadCreateWindow() {
        lblHeader.setText("Create a new box.");
        updateBox = false;
        boxToBeUpdated = null;
        txtNumber.clear();
        txtTitle.clear();
        customerCombobox.setVisible(true);
        customerCombobox.setManaged(true);
        try {
            customerProfileModel = new CustomerProfileModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate CustomerProfileModel.");
        }
        try {
            customerCombobox.getItems().setAll(customerProfileModel.getAllCustomers());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load customers.");
        }
    }

    public void preloadEditWindow(Box box) {
        lblHeader.setText("Edit box.");
        updateBox = true;
        boxToBeUpdated = box;
        txtNumber.setText(String.valueOf(box.getNumber()));
        txtTitle.setText(box.getTitle());
        customerCombobox.setVisible(false);
        customerCombobox.setManaged(false);
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

        if (updateBox) {
            try {
                boxToBeUpdated.setNumber(number);
                boxToBeUpdated.setTitle(title);
                boxDocumentModel.updateBox(boxToBeUpdated);
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to update box.");
                return;
            }
        } else {
            Customer customer = customerCombobox.getSelectionModel().getSelectedItem();

            if (customer == null)
                return;

            Box newBox = new Box(0, number, title, LocalDateTime.now(),
                    loggedInUserId, 0, 0, customer.getId());
            newBox.setCreatedByUsername(loggedInUsername);

            try {
                boxDocumentModel.createBox(newBox);
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to create box.");
                return;
            }
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
        updateBox = false;
        boxToBeUpdated = null;
        txtNumber.clear();
        txtTitle.clear();
    }
}