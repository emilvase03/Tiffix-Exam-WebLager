package dk.easv.tiffixexamweblager.DAL.DAO;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;
import dk.easv.tiffixexamweblager.DAL.IProfileRuleDataAccess;

// Java imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class ProfileRuleDAO implements IProfileRuleDataAccess {
    private DBConnector databaseConnector;

    public ProfileRuleDAO() throws Exception {
        databaseConnector = new DBConnector();
    }

    @Override
    public void addRulesToProfile(Profile profile, List<Rule> rules) throws Exception {
        Connection conn = databaseConnector.getConnection();

        try {
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO ProfileRule (ProfileId, RuleID) VALUES (?,?)");
            for (Rule r : rules) {
                stmt.setInt(1, profile.getId());
                stmt.setInt(2, r.getId());
                stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
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

    @Override
    public void updateRulesForProfile(Profile profile, List<Rule> rules) throws Exception {
        Connection conn = databaseConnector.getConnection();
        try {
         conn.setAutoCommit(false);

         PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM ProfileRule WHERE ProfileId = ?");
         deleteStmt.setInt(1, profile.getId());
         deleteStmt.executeUpdate();

         PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO ProfileRule (ProfileId, RuleID) VALUES (?,?)");
         for (Rule r : rules) {
             insertStmt.setInt(1, profile.getId());
             insertStmt.setInt(2, r.getId());
             insertStmt.addBatch();
         }

         insertStmt.executeBatch();
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
