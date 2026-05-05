package dk.easv.tiffixexamweblager.DAL.DAO;

// Project imports
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BE.Role;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;
import dk.easv.tiffixexamweblager.DAL.IUserDataAccess;

// Java imports
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDataAccess {

    private final DBConnector databaseConnector;

    public UserDAO() throws Exception {
        databaseConnector = new DBConnector();
    }

    @Override
    public List<User> getAllUsers() throws Exception {
        List<User> users = new ArrayList<>();
        String sql = "SELECT Id, FirstName, LastName, Username, Password, RoleId FROM [User] WHERE IsDeleted = 0";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }

        return users;
    }


    @Override
    public User createUser(User newUser) throws Exception {
        String sql = """
                INSERT INTO [User] (FirstName, LastName, Username, Password, RoleId)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, newUser.getFirstName());
            stmt.setString(2, newUser.getLastName());
            stmt.setString(3, newUser.getUsername());
            stmt.setString(4, newUser.getPassword());
            stmt.setInt(5, newUser.getRole().getId());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            int id = -1;

            if (rs.next())
                id = rs.getInt(1);

            return new User(
                    id,
                    newUser.getFirstName(),
                    newUser.getLastName(),
                    newUser.getUsername(),
                    newUser.getPassword(),
                    newUser.getRole()
            );
        }
    }

    @Override
    public void updateUser(User user) throws Exception {

        String sql = """
                UPDATE [User]
                SET FirstName = ?, LastName = ?, Username = ?, Password = ?, RoleId = ?
                WHERE Id = ?
                """;

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getUsername());
            stmt.setString(4, user.getPassword());
            stmt.setInt(5, user.getRole().getId());
            stmt.setInt(6, user.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteUser(User user) throws Exception {
        String sql = "UPDATE [User] SET IsDeleted = 1 WHERE Id = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean usernameExists(String username) throws Exception {
        String sql = "SELECT COUNT(*) FROM [User] WHERE LOWER(Username) = LOWER(?)";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public User getUserByUsername(String username) throws Exception {
        String sql = "SELECT Id, FirstName, LastName, Username, Password, RoleId FROM [User] WHERE username = ? AND IsDeleted = 0;";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
                return mapUser(rs);

            return null;
        }
    }

    @Override
    public List<User> getUsersByRole(Role role) throws Exception {
        String sql = "SELECT Id, FirstName, LastName, Username, Password, RoleId FROM [User] WHERE RoleId = (?) AND IsDeleted = 0;";
        List<User> users = new ArrayList<>();
        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, role.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        }
        return users;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("Id"),
                rs.getString("FirstName"),
                rs.getString("LastName"),
                rs.getString("Username"),
                rs.getString("Password"),
                Role.fromId(rs.getInt("RoleId"))
        );
    }
}