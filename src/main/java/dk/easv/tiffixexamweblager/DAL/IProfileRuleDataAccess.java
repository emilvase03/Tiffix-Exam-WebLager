package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;

// Java imports
import java.util.List;

public interface IProfileRuleDataAccess {
    public void addRulesToProfile(Profile profile, List<Rule> rules) throws Exception;

    public void deleteRulesForProfile(Profile profile) throws Exception;

    public void updateRulesForProfile(Profile profile, List<Rule> rules) throws Exception;
}
