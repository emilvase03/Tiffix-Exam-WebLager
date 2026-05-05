package dk.easv.tiffixexamweblager.DAL.DAO;

import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.UserProfile;
import dk.easv.tiffixexamweblager.DAL.IUserProfileDataAccess;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserProfileDAO implements IUserProfileDataAccess {

    private final DBConnector dbConnector;

    public UserProfileDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    @Override
    public List<UserProfile> getEmployeesForProfile(int profileId) throws Exception {
        List<UserProfile> list = new ArrayList<>();
        String sql = "SELECT * FROM UserProfile WHERE ProfileId = ? AND IsDeleted = 0";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new UserProfile(
                        rs.getInt("Id"),
                        rs.getInt("UserId"),
                        rs.getInt("ProfileId")
                ));
            }
        }
        return list;
    }

    @Override
    public List<Profile> getProfilesForEmployee(int userId) throws Exception {
        List<Profile> profiles = new ArrayList<>();
        String sql = """
        SELECT p.Id, p.Title
        FROM Profile p
        JOIN UserProfile up ON p.Id = up.ProfileId
        WHERE up.UserId = ? AND p.IsDeleted = 0;
    """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Profile profile = new Profile(rs.getString("Title"));
                profile.setId(rs.getInt("Id"));
                profiles.add(profile);
            }
        }
        return profiles;
    }
    @Override
    public void assignEmployees(int userId, int profileId) throws Exception {
        String sql = "INSERT INTO UserProfile (UserId, ProfileId) VALUES (?, ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, profileId);
            ps.executeUpdate();
        }
    }

    @Override
    public void removeEmployees(int userId, int profileId) throws Exception {
        String sql = "UPDATE UserProfile SET IsDeleted = 1 WHERE UserId = ? AND ProfileId = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, profileId);
            ps.executeUpdate();
        }
    }
}
