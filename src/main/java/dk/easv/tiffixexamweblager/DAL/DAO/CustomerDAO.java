package dk.easv.tiffixexamweblager.DAL.DAO;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;
import dk.easv.tiffixexamweblager.DAL.ICustomerDataAccess;

// Java imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO implements ICustomerDataAccess {

        private final DBConnector databaseConnector;

        public CustomerDAO() throws Exception {
            databaseConnector = new DBConnector();
        }

        @Override
        public List<Customer> getAllCustomers() throws Exception {
            List<Customer> customers = new ArrayList<>();

            String sql = "SELECT Id, Name FROM Customer";

            try (Connection conn = databaseConnector.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("Id");
                    String name = rs.getString("Name");
                    Customer c = new Customer(name);
                    c.setId(id);
                    customers.add(c);
                }
            }
            return customers;

        }

    @Override
    public Customer createCustomer(Customer newCustomer) throws Exception {
        String sql = "INSERT INTO Customer (Name) VALUES (?)";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, newCustomer.getName());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            int id = -1;

            if (rs.next())
                id = rs.getInt(1);

            Customer c = new Customer(newCustomer.getName());
            c.setId(id);
            return c;
        }
    }

    @Override
    public void updateCustomer(Customer customer) throws Exception {
        String sql = """
                UPDATE Customer
                SET Name = ?
                WHERE id = ?
                """;

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getName());
            stmt.setInt(2, customer.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteCustomer(Customer customer) throws Exception {
        String sql = "DELETE FROM Customer WHERE id = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customer.getId());
            stmt.executeUpdate();
        }
    }

}
