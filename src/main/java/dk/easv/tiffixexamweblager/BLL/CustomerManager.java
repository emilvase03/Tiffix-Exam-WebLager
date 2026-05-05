package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.DAL.DAO.CustomerDAO;
import dk.easv.tiffixexamweblager.DAL.ICustomerDataAccess;

// Java imports
import java.util.List;

public class CustomerManager {
    private ICustomerDataAccess dataAccess;

    public CustomerManager() throws Exception{
        dataAccess = new CustomerDAO();
    }

    public List<Customer> getAllCustomers() throws Exception {
        return dataAccess.getAllCustomers();
    }

    public Customer createCustomer(Customer newCustomer) throws Exception {
        return dataAccess.createCustomer(newCustomer);
    }

    public void updateCustomer(Customer customer) throws Exception {
        dataAccess.updateCustomer(customer);
    }

    public void deleteCustomer(Customer customer) throws Exception {
        dataAccess.deleteCustomer(customer);
    }
}
