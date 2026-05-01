package dk.easv.tiffixexamweblager.DAL.DAO;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;
import dk.easv.tiffixexamweblager.DAL.IRuleDataAccess;

// Java imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RuleDAO implements IRuleDataAccess {
    private DBConnector databaseConnector;

    public RuleDAO() throws Exception {
        databaseConnector = new DBConnector();
    }

    @Override
    public List<Rule> getAllRules() throws Exception {
        List<Rule> allRules = new ArrayList<>();

        String sql = "SELECT * FROM [Rule]";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("Id");
                String name = rs.getString("Name");
                int amount = rs.getInt("Amount");
                Rule rule = new Rule(id, name, amount);
                allRules.add(rule);
            }
        }

        return allRules;
    }

    @Override
    public List<Rule> getRulesForProfile(Profile profile) throws Exception {
        List<Rule> rules = new ArrayList<>();

        String sql = """
                SELECT [Rule].Id, [Rule].Name, [Rule].Amount FROM [Rule]
                JOIN ProfileRule ON [Rule].Id = ProfileRule.RuleId
                JOIN Profile ON ProfileRule.ProfileId = Profile.Id
                WHERE Profile.Id = ?;
                """;

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profile.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("Id");
                String name = rs.getString("Name");
                int amount = rs.getInt("Amount");

                Rule rule = new Rule(id, name, amount);
                rules.add(rule);
            }
        }

        return rules;
    }
}
