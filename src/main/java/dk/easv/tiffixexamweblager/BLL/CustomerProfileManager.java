package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.DAL.DAO.CustomerProfileDAO;
import dk.easv.tiffixexamweblager.DAL.ICustomerProfileDataAccess;

public class CustomerProfileManager {
    private ICustomerProfileDataAccess dataAccess;

    public CustomerProfileManager() throws Exception {
        dataAccess = new CustomerProfileDAO();
    }

    public Customer getCustomerForProfile(Profile profile) throws Exception {
        return dataAccess.getCustomerForProfile(profile);
    }

    public void addProfileToCustomer(Customer customer, Profile profile) throws Exception {
        dataAccess.addProfileToCustomer(customer, profile);
    }

    public void updateProfileForCustomer(Customer customer, Profile profile) throws Exception {
        dataAccess.updateProfileForCustomer(customer, profile);
    }
}
