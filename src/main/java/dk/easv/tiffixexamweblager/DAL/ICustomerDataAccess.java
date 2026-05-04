package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;

// Java imports
import java.util.List;

public interface ICustomerDataAccess {
    public List<Customer> getAllCustomers() throws Exception;

    public Customer createCustomer(Customer newCustomer) throws Exception;

    public void updateCustomer(Customer customer) throws Exception;

    public void deleteCustomer(Customer customer) throws Exception;
}
