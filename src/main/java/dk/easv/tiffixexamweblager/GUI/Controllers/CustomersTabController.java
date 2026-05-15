package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.CustomerCardController;
import dk.easv.tiffixexamweblager.GUI.Models.CustomerProfileModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// Ikonli imports
import org.kordamp.ikonli.javafx.FontIcon;

// Java imports
import javafx.fxml.Initializable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.util.ResourceBundle;

public class CustomersTabController implements Initializable {
    @FXML private TableView<Customer> tblCustomer;
    @FXML private TableColumn<Customer, String> colTitle;
    @FXML private TableColumn<Customer, Void> colManage;
    @FXML private TableColumn<Customer, Boolean> colActive;
    @FXML private VBox customerCardOverlay;
    @FXML private CustomerCardController customerCardController;

    private CustomerProfileModel customerProfileModel;

    public CustomersTabController() {
        try {
            customerProfileModel = new CustomerProfileModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to initialize CustomerModel.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupActiveColumn();
        setupManageColumn();

        customerCardController.setOverlay(customerCardOverlay);
        customerCardController.setCustomersTabController(this);
    }

    private void setupTable() {
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        try {
            tblCustomer.setItems(customerProfileModel.getAllObservableIncludingSoftDeleted());
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
            private final HBox container  = new HBox(8, btnDelete);

            {
                btnDelete.setGraphic(new FontIcon("bi-trash"));
                btnDelete.getStyleClass().addAll("icon-button", "danger");
                btnDelete.setOnAction(e ->
                        handleDeleteCustomer(tblCustomer.getItems().get(getIndex()))
                );

                container.setAlignment(Pos.CENTER);
            }

            private void handleDeleteCustomer(Customer customer) {
                if (customer == null) return;

                boolean confirmed = AlertHelper.showConfirmation(
                        "Deactivate Customer",
                        "Are you sure you want to deactivate \"" + customer.getName() + "\"?"
                );
                if (!confirmed) return;

                try {
                    customerProfileModel.deleteCustomer(customer);
                    tblCustomer.getItems().remove(customer);
                } catch (Exception e) {
                    AlertHelper.showError("Error", "Failed to deactivate customer.");
                }
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
                new SimpleBooleanProperty(data.getValue().getIsDeleted())
        );

        colActive.setCellFactory(col -> new TableCell<>() {
            final Button btnActive = new Button();
            final Button btnDeactivate = new Button();
            final HBox container = new HBox();
            {
                btnActive.setGraphic(new FontIcon("bi-check-square"));
                btnActive.getStyleClass().addAll("icon-button");
                btnActive.setOnAction(e -> {
                    // Not implemented yet
                });

                btnDeactivate.setGraphic(new FontIcon("bi-dash-square"));
                btnDeactivate.getStyleClass().addAll("icon-button", "danger");
                btnDeactivate.setOnAction(e -> {
                    // Not implemented yet
                });

                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Boolean isDeleted, boolean empty) {
                super.updateItem(isDeleted, empty);
                if (empty || isDeleted == null) {
                    setGraphic(null);
                    return;
                }
                container.getChildren().setAll(isDeleted ? btnDeactivate : btnActive);
                setGraphic(container);
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
