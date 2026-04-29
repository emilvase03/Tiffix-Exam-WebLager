package dk.easv.tiffixexamweblager.BLL;

import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.UserProfile;
import dk.easv.tiffixexamweblager.DAL.DAO.UserProfileDAO;
import dk.easv.tiffixexamweblager.DAL.IUserProfileDataAccess;

import java.util.List;

public class UserProfileManager {

    private final IUserProfileDataAccess userProfileDAO;

    public UserProfileManager() throws Exception {
        userProfileDAO = new UserProfileDAO();
    }

     public List<UserProfile> getCoordinatorsForEvent(int profileId) throws Exception {
        return userProfileDAO.getEmployeesForProfile(profileId);
    }

    public void assignCoordinator(int userId, int profileId) throws Exception {
        userProfileDAO.assignEmployees(userId, profileId);
    }

    public void removeCoordinator(int userId, int profileId) throws Exception {
        userProfileDAO.removeEmployees(userId, profileId);
    }

    public List<Profile> getProfilesForEmployee(int userId) throws Exception {
        return userProfileDAO.getProfilesForEmployee(userId);
    }
}