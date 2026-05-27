package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BE.UserProfile;
import dk.easv.tiffixexamweblager.BLL.Utils.LogAction;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.DAL.DAO.UserProfileDAO;
import dk.easv.tiffixexamweblager.DAL.IUserProfileDataAccess;

// Java imports
import java.util.List;

public class UserProfileManager {

    private final IUserProfileDataAccess dataAccess;
    private final LogManager             logManager;

    public UserProfileManager() throws Exception {
        dataAccess = new UserProfileDAO();
        logManager = new LogManager();
    }

    public List<UserProfile> getEmployeesForProfile(int profileId) throws Exception {
        return dataAccess.getEmployeesForProfile(profileId);
    }

    public void assignEmployees(int userId, int profileId) throws Exception {
        dataAccess.assignEmployees(userId, profileId);
        log(LogAction.CREATE, "Assigned user ID: " + userId + " to profile ID: " + profileId);
    }

    public void removeEmployees(int userId, int profileId) throws Exception {
        dataAccess.removeEmployees(userId, profileId);
        log(LogAction.DELETE, "Removed user ID: " + userId + " from profile ID: " + profileId);
    }

    public List<Profile> getProfilesForEmployee(int userId) throws Exception {
        return dataAccess.getProfilesForEmployee(userId);
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