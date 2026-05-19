package dk.easv.tiffixexamweblager.BLL;

import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BLL.Utils.LogAction;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.DAL.DAO.ProfileDAO;
import dk.easv.tiffixexamweblager.DAL.IProfileDataAccess;

import java.util.List;

public class ProfileManager {

    private IProfileDataAccess dataAccess;
    private final LogManager   logManager;

    public ProfileManager() throws Exception {
        dataAccess = new ProfileDAO();
        logManager = new LogManager();
    }

    public List<Profile> getAllProfiles() throws Exception {
        return dataAccess.getAll();
    }

    public Profile createProfile(Profile newProfile) throws Exception {
        Profile created = dataAccess.create(newProfile);
        log(LogAction.CREATE, "Created profile: " + newProfile.getTitle());
        return created;
    }

    public void updateProfile(Profile profile) throws Exception {
        dataAccess.update(profile);
        log(LogAction.UPDATE, "Updated profile: " + profile.getTitle());
    }

    public void deleteProfile(Profile profile) throws Exception {
        dataAccess.delete(profile);
        log(LogAction.DELETE, "Deleted profile: " + profile.getTitle());
    }

    public List<Profile> getAllIncludingSoftDeleted() throws Exception {
        return dataAccess.getAllIncludingSoftDeleted();
    }

    public boolean toggleSoftDelete(int id) throws Exception {
        boolean result = dataAccess.toggleSoftDelete(id);
        log(LogAction.UPDATE, "Toggled active status for profile ID: " + id);
        return result;
    }

    private void log(LogAction action, String message) {
        try {
            User current = UserSession.getInstance().getCurrentUser();
            if (current != null) {
                logManager.log(
                        current.getId(),
                        action,
                        current.getUsername() + ": " + message
                );
            }
        } catch (Exception ignored) {}
    }
}