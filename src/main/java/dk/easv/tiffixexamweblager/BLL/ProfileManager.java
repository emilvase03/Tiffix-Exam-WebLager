package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.DAL.IProfileDataAccess;
import dk.easv.tiffixexamweblager.DAL.DAO.ProfileDAO;

// Java imports
import java.util.List;

public class ProfileManager {
    private IProfileDataAccess dataAccess;

    public ProfileManager() throws Exception {
        dataAccess = new ProfileDAO();
    }

    public List<Profile> getAllProfiles() throws Exception {
        return dataAccess.getAllProfiles();
    };

    public Profile createProfile(Profile newProfile) throws Exception {
        return dataAccess.createProfile(newProfile);
    };

    public void updateProfile(Profile profile) throws Exception {
        dataAccess.updateProfile(profile);
    };

    public void deleteProfile(Profile profile) throws Exception {
        dataAccess.deleteProfile(profile);
    };
}
