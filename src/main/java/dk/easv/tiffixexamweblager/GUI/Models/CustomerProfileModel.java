package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BLL.CustomerProfileManager;

public class CustomerProfileModel {
    private CustomerProfileManager manager;

    public CustomerProfileModel() throws Exception {
        manager = new CustomerProfileManager();
    }

    public Customer getCustomerForProfile(Profile profile) throws Exception {
        return manager.getCustomerForProfile(profile);
    }

    public void addProfileToCustomer(Customer customer, Profile profile) throws Exception {
        manager.addProfileToCustomer(customer, profile);
    }

    public void updateProfileForCustomer(Customer customer, Profile profile) throws Exception {
        manager.updateProfileForCustomer(customer, profile);
    }
}
