package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.BLL.ProfileRuleManager;

import java.util.List;

public class ProfileRuleModel {
    private ProfileRuleManager manager;

    public ProfileRuleModel() throws Exception{
        manager = new ProfileRuleManager();
    }

    public void addRulesToProfile(Profile profile, List<Rule> rules) throws Exception {
        manager.addRulesToProfile(profile, rules);
    }

    public void deleteRulesForProfile(Profile profile) throws Exception {
        manager.deleteRulesForProfile(profile);
    }

    public void updateRulesForProfile(Profile profile, List<Rule> rules) throws Exception {
        manager.updateRulesForProfile(profile, rules);
    }
}
