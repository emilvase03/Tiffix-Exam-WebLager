package dk.easv.tiffixexamweblager.DAL.DAO;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;
import dk.easv.tiffixexamweblager.DAL.IProfileDataAccess;

// Java imports
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO implements IProfileDataAccess {

        private final DBConnector databaseConnector;

        public ProfileDAO() throws Exception {
            databaseConnector = new DBConnector();
        }

        @Override
        public List<Profile> getAllProfiles() throws Exception {

            List<Profile> profiles = new ArrayList<>();

            String sql = "SELECT Id, Title FROM Profile WHERE IsDeleted = 0";

            try (Connection conn = databaseConnector.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("Id");
                    String title = rs.getString("Title");
                    Profile p = new Profile(title);
                    p.setId(id);
                    profiles.add(p);
                }
            }

            return profiles;

        }

    @Override
    public Profile createProfile(Profile newProfile) throws Exception {
        String sql = "INSERT INTO Profile (Title) VALUES (?)";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, newProfile.getTitle());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            int id = -1;

            if (rs.next())
                id = rs.getInt(1);

            Profile p = new Profile(newProfile.getTitle());
            p.setId(id);
            return p;
        }
    }

    @Override
    public void updateProfile(Profile profile) throws Exception {
        String sql = """
                UPDATE Profile
                SET Title = ?
                WHERE id = ?
                """;

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, profile.getTitle());
            stmt.setInt(2, profile.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteProfile(Profile profile) throws Exception {
        String sql = "UPDATE Profile SET IsDeleted = 1 WHERE Id = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profile.getId());
            stmt.executeUpdate();
        }
    }

}
