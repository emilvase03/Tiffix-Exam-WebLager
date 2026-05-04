package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.CustomerCardController;
import dk.easv.tiffixexamweblager.GUI.Models.CustomerModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// Ikonli imports
import org.kordamp.ikonli.javafx.FontIcon;

// Java imports
import javafx.fxml.Initializable;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class CustomersTabController implements Initializable {
    @FXML private TableView<Customer> tblCustomer;
    @FXML private TableColumn<Customer, String> colTitle;
    @FXML private TableColumn<Customer, Void> colManage;
    @FXML private VBox customerCardOverlay;
    @FXML private CustomerCardController customerCardController;

    private CustomerModel customerModel;

    public CustomersTabController() {
        try {
            customerModel = new CustomerModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to initialize CustomerModel.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupManageColumn();

        customerCardController.setOverlay(customerCardOverlay);
        customerCardController.setCustomersTabController(this);
    }

    private void setupTable() {
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        try {
            tblCustomer.setItems(customerModel.getAllCustomers());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to retrieve customers from database.");
        }

        tblCustomer.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showCreateOverlay();
                    customerCardController.preloadUpdateWindow(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupManageColumn() {
        colManage.setCellFactory(col -> new TableCell<>() {

            private final Button btnDelete = new Button();

            {
                btnDelete.setGraphic(new FontIcon("bi-trash"));
                btnDelete.getStyleClass().addAll("icon-button", "danger");
                btnDelete.setOnAction(e ->
                        handleDeleteCustomer(tblCustomer.getItems().get(getIndex()))
                );
            }

            private void handleDeleteCustomer(Customer customer) {
                if (customer == null) return;

                boolean confirmed = AlertHelper.showConfirmation(
                        "Delete Customer",
                        "Are you sure you want to delete \"" + customer.getName() + "\"?"
                );
                if (!confirmed) return;

                try {
                    customerModel.deleteCustomer(customer);
                    tblCustomer.getItems().remove(customer);
                } catch (Exception e) {
                    AlertHelper.showError("Error", "Failed to delete customer.");
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });
    }

    @FXML
    private void handleCreateCustomer(ActionEvent event) {
        showCreateOverlay();
        customerCardController.preloadCreateWindow();
    }

    private void showCreateOverlay() {
        customerCardOverlay.setVisible(true);
        customerCardOverlay.setManaged(true);
    }

    public TableView<Customer> getTable() {
        return tblCustomer;
    }
}
