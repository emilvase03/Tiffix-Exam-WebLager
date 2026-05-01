package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.DAL.DAO.ProfileRuleDAO;
import dk.easv.tiffixexamweblager.DAL.IProfileRuleDataAccess;

public class ProfileRuleManager {
    private IProfileRuleDataAccess dataAccess;

    public ProfileRuleManager() throws Exception {
        dataAccess = new ProfileRuleDAO();
    }

    public void addRuleToProfile(Profile profile, Rule rule) throws Exception {
        dataAccess.addRuleToProfile(profile, rule);
    }

    public void deleteRulesForProfile(Profile profile) throws Exception {
        dataAccess.deleteRulesForProfile(profile);
    }
}
