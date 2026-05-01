package dk.easv.tiffixexamweblager.DAL.DAO;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;
import dk.easv.tiffixexamweblager.DAL.IProfileRuleDataAccess;

// Java imports
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ProfileRuleDAO implements IProfileRuleDataAccess {
    private DBConnector databaseConnector;

    public ProfileRuleDAO() throws Exception {
        databaseConnector = new DBConnector();
    }

    @Override
    public void addRuleToProfile(Profile profile, Rule rule) throws Exception {
        String sql = "INSERT INTO ProfileRule (ProfileId, RuleID) VALUES (?,?)";

        try(Connection connection = databaseConnector.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, profile.getId());
            stmt.setInt(2, rule.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteRulesForProfile(Profile profile) throws Exception {
        String sql = "DELETE FROM ProfileRule WHERE ProfileId = ?";

        try (Connection conn = databaseConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profile.getId());
            stmt.executeUpdate();
        }
    }
}
