package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BLL.Utils.LogAction;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.DAL.DAO.CustomerDAO;
import dk.easv.tiffixexamweblager.DAL.ICustomerDataAccess;

// Java imports
import java.util.List;

public class CustomerManager {

    private ICustomerDataAccess dataAccess;
    private final LogManager    logManager;

    public CustomerManager() throws Exception {
        dataAccess = new CustomerDAO();
        logManager = new LogManager();
    }

    public List<Customer> getAllCustomers() throws Exception {
        return dataAccess.getAll();
    }

    public Customer createCustomer(Customer newCustomer) throws Exception {
        Customer created = dataAccess.create(newCustomer);
        log(LogAction.CREATE, "Created customer: " + newCustomer.getName());
        return created;
    }

    public void updateCustomer(Customer customer) throws Exception {
        dataAccess.update(customer);
        log(LogAction.UPDATE, "Updated customer: " + customer.getName());
    }

    public void deleteCustomer(Customer customer) throws Exception {
        dataAccess.delete(customer);
        log(LogAction.DELETE, "Deleted customer: " + customer.getName());
    }

    public List<Customer> getAllIncludingSoftDeleted() throws Exception {
        return dataAccess.getAllIncludingSoftDeleted();
    }

    public boolean toggleSoftDelete(int id) throws Exception {
        boolean result = dataAccess.toggleSoftDelete(id);
        log(LogAction.UPDATE, "Toggled active status for customer ID: " + id);
        return result;
    }

    private void log(LogAction action, String message) {
        try {
            User current = UserSession.getInstance().getCurrentUser();
            if (current != null) {
                logManager.log(
                        current.getId(),
                        action,
                        current.getUsername() + ": " + message
                );
            }
        } catch (Exception ignored) {}
    }
}