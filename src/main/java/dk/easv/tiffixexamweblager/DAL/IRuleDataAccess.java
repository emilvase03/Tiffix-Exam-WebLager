package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;

// Java imports
import java.util.List;

public interface IRuleDataAccess {
    public List<Rule> getAllRules() throws Exception;

    public List<Rule> getRulesForProfile(Profile profile) throws Exception;
}
