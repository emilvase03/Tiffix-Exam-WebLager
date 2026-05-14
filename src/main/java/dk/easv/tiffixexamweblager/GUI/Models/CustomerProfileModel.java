package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BLL.CustomerManager;
import dk.easv.tiffixexamweblager.BLL.CustomerProfileManager;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class CustomerProfileModel {
    private CustomerProfileManager customerProfileManager;
    private CustomerManager customerManager;
    private ObservableList<Customer> allCustomers = FXCollections.observableArrayList();
    private ObservableList<Customer> allTrueCustomers = FXCollections.observableArrayList();

    public CustomerProfileModel() throws Exception {
        customerProfileManager = new CustomerProfileManager();
        customerManager = new CustomerManager();
    }

    // CustomerProfileManager
    public Customer getCustomerForProfile(Profile profile) throws Exception {
        return customerProfileManager.getCustomerForProfile(profile);
    }

    public void addProfileToCustomer(Customer customer, Profile profile) throws Exception {
        customerProfileManager.addProfileToCustomer(customer, profile);
    }

    public void updateProfileForCustomer(Customer customer, Profile profile) throws Exception {
        customerProfileManager.updateProfileForCustomer(customer, profile);
    }

    // CustomerManager
    public ObservableList<Customer> getAllCustomers() throws Exception {
        allCustomers.setAll(customerManager.getAllCustomers());
        return allCustomers;
    }

    public Customer createCustomer(Customer newCustomer) throws Exception {
        return customerManager.createCustomer(newCustomer);
    }

    public void updateCustomer(Customer customer) throws Exception {
        customerManager.updateCustomer(customer);
    }

    public void deleteCustomer(Customer customer) throws Exception {
        customerManager.deleteCustomer(customer);
    }

    public ObservableList<Customer> getTrueAll() throws Exception {
        allTrueCustomers.setAll(customerManager.getTrueAll());
        return allTrueCustomers;
    }
}
