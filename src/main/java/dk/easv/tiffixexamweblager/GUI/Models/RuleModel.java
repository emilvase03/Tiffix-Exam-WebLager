package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.BLL.RuleManager;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RuleModel {
    private RuleManager manager;
    private ObservableList<Rule> allRules = FXCollections.observableArrayList();
    private ObservableList<Rule> profileRules = FXCollections.observableArrayList();

    public RuleModel() throws Exception{
        manager = new RuleManager();
    }

    public ObservableList<Rule> getAllRules() throws Exception {
        allRules.setAll(manager.getAllRules());
        return allRules;
    }

    public ObservableList<Rule> getRulesForProfile(Profile profile) throws Exception {
        profileRules.setAll(manager.getRulesForProfile(profile));
        return profileRules;
    }
}
