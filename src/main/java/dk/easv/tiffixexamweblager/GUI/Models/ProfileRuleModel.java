package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.BLL.ProfileManager;
import dk.easv.tiffixexamweblager.BLL.ProfileRuleManager;
import dk.easv.tiffixexamweblager.BLL.RuleManager;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class ProfileRuleModel {
    private ProfileManager profileManager;
    private ProfileRuleManager profileRuleManager;
    private RuleManager ruleManager;
    private ObservableList<Profile> allProfiles = FXCollections.observableArrayList();
    private ObservableList<Rule> allRules = FXCollections.observableArrayList();
    private ObservableList<Rule> profileRules = FXCollections.observableArrayList();

    public ProfileRuleModel() throws Exception {
        profileManager = new ProfileManager();
        profileRuleManager = new ProfileRuleManager();
        ruleManager = new RuleManager();
    }

    // ProfileManager
    public ObservableList<Profile> getAllTrueObservableProfiles() throws Exception {
        allProfiles.setAll(profileManager.getTrueAll());
        return allProfiles;
    };

    public Profile createProfile(Profile newProfile) throws Exception {
        return profileManager.createProfile(newProfile);
    };

    public void updateProfile(Profile profile) throws Exception {
        profileManager.updateProfile(profile);
    };

    public void deleteProfile(Profile profile) throws Exception {
        profileManager.deleteProfile(profile);
    };

    // ProfileRuleManager
    public void addRulesToProfile(Profile profile, List<Rule> rules) throws Exception {
        profileRuleManager.addRulesToProfile(profile, rules);
    }

    public void updateRulesForProfile(Profile profile, List<Rule> rules) throws Exception {
        profileRuleManager.updateRulesForProfile(profile, rules);
    }

    // RuleManager
    public ObservableList<Rule> getAllRules() throws Exception {
        allRules.setAll(ruleManager.getAllRules());
        return allRules;
    }

    public ObservableList<Rule> getRulesForProfile(Profile profile) throws Exception {
        profileRules.setAll(ruleManager.getRulesForProfile(profile));
        return profileRules;
    }
}
