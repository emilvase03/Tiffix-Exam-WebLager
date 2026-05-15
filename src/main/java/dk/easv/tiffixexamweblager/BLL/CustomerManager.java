package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.DAL.DAO.CustomerDAO;
import dk.easv.tiffixexamweblager.DAL.ICRUDDataAccess;
import dk.easv.tiffixexamweblager.DAL.ICustomerDataAccess;

// Java imports
import java.util.List;

public class CustomerManager {
    private ICustomerDataAccess dataAccess;

    public CustomerManager() throws Exception{
        dataAccess = new CustomerDAO();
    }

    public List<Customer> getAllCustomers() throws Exception {
        return dataAccess.getAll();
    }

    public Customer createCustomer(Customer newCustomer) throws Exception {
        return dataAccess.create(newCustomer);
    }

    public void updateCustomer(Customer customer) throws Exception {
        dataAccess.update(customer);
    }

    public void deleteCustomer(Customer customer) throws Exception {
        dataAccess.delete(customer);
    }

    public List<Customer> getAllIncludingSoftDeleted() throws Exception {
        return dataAccess.getAllIncludingSoftDeleted();
    }
}
