package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.DAL.DAO.RuleDAO;
import dk.easv.tiffixexamweblager.DAL.IRuleDataAccess;

// Java imports
import java.util.List;

public class RuleManager {
    private IRuleDataAccess dataAccess;

    public RuleManager() throws Exception{
        dataAccess = new RuleDAO();
    }

    public List<Rule> getAllRules() throws Exception{
        return dataAccess.getAllRules();
    }

    public List<Rule> getRulesForProfile(Profile profile) throws Exception {
        return dataAccess.getRulesForProfile(profile);
    }
}
