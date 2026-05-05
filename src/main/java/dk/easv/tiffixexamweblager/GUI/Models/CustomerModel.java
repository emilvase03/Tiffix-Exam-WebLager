package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.BLL.CustomerManager;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CustomerModel {
    private CustomerManager manager;
    private ObservableList<Customer> allCustomers = FXCollections.observableArrayList();

    public CustomerModel() throws Exception{
        manager = new CustomerManager();
    }

    public ObservableList<Customer> getAllCustomers() throws Exception {
        allCustomers.setAll(manager.getAllCustomers());
        return allCustomers;
    }

    public Customer createCustomer(Customer newCustomer) throws Exception {
        return manager.createCustomer(newCustomer);
    }

    public void updateCustomer(Customer customer) throws Exception {
        manager.updateCustomer(customer);
    }

    public void deleteCustomer(Customer customer) throws Exception {
        manager.deleteCustomer(customer);
    }
}
