package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.GUI.Controllers.CustomersTabController;
import dk.easv.tiffixexamweblager.GUI.Models.CustomerModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// Java imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class CustomerCardController {
    @FXML private TextField txtName;
    @FXML private Label lblHeader;

    private VBox overlay;
    private CustomersTabController customersTabController;
    private boolean updateCustomer = false;
    private Customer customerToBeUpdated;
    private CustomerModel customerModel;

    public CustomerCardController() {
        try {
            customerModel = new CustomerModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate CustomerModel.");
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (txtName.getText().isBlank()) {
            txtName.setStyle("-fx-border-color: #FF3D32; -fx-border-width: 1;");
            return;
        }

        // If update vs If create
        if (updateCustomer) {
            try {
                if (customerToBeUpdated != null) {
                    customerToBeUpdated.setName(txtName.getText().trim());
                    customerModel.updateCustomer(customerToBeUpdated);

                    customersTabController.getTable().refresh();
                    txtName.clear();
                    lblHeader.setText("Create a new customer.");
                    handleClose();
                    updateCustomer = false;
                }
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to update customer.");
            }
        } else {
            try {
                Customer newCustomer = customerModel.createCustomer(new Customer(txtName.getText().trim()));
                customersTabController.getTable().getItems().add(newCustomer);

                txtName.clear();
                handleClose();
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to create new customer.");
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        handleClose();
    }

    public void setOverlay(VBox overlay) {
        this.overlay = overlay;
    }

    public void setCustomersTabController(CustomersTabController customersTabController) {
        this.customersTabController = customersTabController;
    }

    public void preloadUpdateWindow(Customer customer) {
        txtName.setText(customer.getName());
        lblHeader.setText("Edit customer.");
        updateCustomer = true;
        customerToBeUpdated = customer;
    }

    public void preloadCreateWindow() {
        lblHeader.setText("Create a new customer.");
        updateCustomer = false;
    }

    private void handleClose() {
        if (overlay != null) {
            updateCustomer = false;
            txtName.clear();
            txtName.setStyle("");
            overlay.setVisible(false);
            overlay.setManaged(false);
        }
    }

}
