package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.DAL.DAO.ProfileDAO;
import dk.easv.tiffixexamweblager.DAL.IProfileDataAccess;

// Java imports
import java.util.List;

public class ProfileManager {
    private IProfileDataAccess dataAccess;

    public ProfileManager() throws Exception {
        dataAccess = new ProfileDAO();
    }

    public List<Profile> getAllProfiles() throws Exception {
        return dataAccess.getAll();
    };

    public Profile createProfile(Profile newProfile) throws Exception {
        return dataAccess.create(newProfile);
    };

    public void updateProfile(Profile profile) throws Exception {
        dataAccess.update(profile);
    };

    public void deleteProfile(Profile profile) throws Exception {
        dataAccess.delete(profile);
    };

    public List<Profile> getTrueAll() throws Exception {
        return dataAccess.getTrueAll();
    }
}
