package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.BLL.ProfileRuleManager;

public class ProfileRuleModel {
    private ProfileRuleManager manager;

    public ProfileRuleModel() throws Exception{
        manager = new ProfileRuleManager();
    }

    public void addRuleToProfile(Profile profile, Rule rule) throws Exception {
        manager.addRuleToProfile(profile, rule);
    }

    public void deleteRulesForProfile(Profile profile) throws Exception {
        manager.deleteRulesForProfile(profile);
    }
}
