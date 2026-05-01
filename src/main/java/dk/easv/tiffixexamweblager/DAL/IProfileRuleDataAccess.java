package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;

public interface IProfileRuleDataAccess {
    public void addRuleToProfile(Profile profile, Rule rule) throws Exception;

    public void deleteRulesForProfile(Profile profile) throws Exception;
}
