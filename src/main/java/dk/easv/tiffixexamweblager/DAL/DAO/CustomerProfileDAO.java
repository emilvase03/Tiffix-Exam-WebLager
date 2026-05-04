package dk.easv.tiffixexamweblager.DAL.DAO;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;
import dk.easv.tiffixexamweblager.DAL.ICustomerProfileDataAccess;

// Java imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerProfileDAO implements ICustomerProfileDataAccess {

    private final DBConnector databaseConnector;

    public CustomerProfileDAO() throws Exception {
        databaseConnector = new DBConnector();
    }

    @Override
    public Customer getCustomerForProfile(Profile profile) throws Exception {
        String sql = """
                    SELECT Customer.Id, Customer.Name FROM Customer
                    JOIN CustomerProfile ON Customer.Id = CustomerProfile.CustomerId
                    JOIN Profile ON CustomerProfile.ProfileId = Profile.Id
                    WHERE Profile.Id = ?;
                    """;

        try (Connection conn = databaseConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, profile.getId());

            ResultSet rs = stmt.executeQuery();

            int id = -1;
            if (rs.next())
                id = rs.getInt("Id");

            Customer c = new Customer(rs.getString("Name"));
            c.setId(id);
            return c;
        }
    }

    @Override
    public void addProfileToCustomer(Customer customer, Profile profile) throws Exception {
        String sql = "INSERT INTO CustomerProfile (CustomerId, ProfileId) VALUES (?,?)";

        try (Connection conn = databaseConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customer.getId());
            stmt.setInt(2, profile.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void updateProfileForCustomer(Customer customer, Profile profile) throws Exception {
        Connection conn = databaseConnector.getConnection();

        try {
            conn.setAutoCommit(false);

            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM CustomerProfile WHERE CustomerId = ?");
            deleteStmt.setInt(1, customer.getId());
            deleteStmt.executeUpdate();

            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO CustomerProfile (CustomerId, ProfileId) VALUES (?,?)");
            insertStmt.setInt(1, customer.getId());
            insertStmt.setInt(2, profile.getId());
            insertStmt.executeUpdate();

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
