package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.DAL.DAO.ProfileRuleDAO;
import dk.easv.tiffixexamweblager.DAL.IProfileRuleDataAccess;

import java.util.List;

public class ProfileRuleManager {
    private IProfileRuleDataAccess dataAccess;

    public ProfileRuleManager() throws Exception {
        dataAccess = new ProfileRuleDAO();
    }

    public void addRulesToProfile(Profile profile, List<Rule> rules) throws Exception {
        dataAccess.addRulesToProfile(profile, rules);
    }

    public void deleteRulesForProfile(Profile profile) throws Exception {
        dataAccess.deleteRulesForProfile(profile);
    }

    public void updateRulesForProfile(Profile profile, List<Rule> rules) throws Exception {
        dataAccess.updateRulesForProfile(profile, rules);
    }
}
