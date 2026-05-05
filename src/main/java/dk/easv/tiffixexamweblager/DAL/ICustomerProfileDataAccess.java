package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.BE.Profile;

public interface ICustomerProfileDataAccess {
    public Customer getCustomerForProfile(Profile profile) throws Exception;

    public void addProfileToCustomer(Customer customer, Profile profile) throws Exception;

    public void updateProfileForCustomer(Customer customer, Profile profile) throws Exception;
}
