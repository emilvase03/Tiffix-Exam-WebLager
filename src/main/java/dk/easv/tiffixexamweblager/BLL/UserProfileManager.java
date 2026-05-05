package dk.easv.tiffixexamweblager.BLL;

import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.UserProfile;
import dk.easv.tiffixexamweblager.DAL.DAO.UserProfileDAO;
import dk.easv.tiffixexamweblager.DAL.IUserProfileDataAccess;

import java.util.List;

public class UserProfileManager {

    private final IUserProfileDataAccess dataAccess;

    public UserProfileManager() throws Exception {
        dataAccess = new UserProfileDAO();
    }

     public List<UserProfile> getCoordinatorsForEvent(int profileId) throws Exception {
        return dataAccess.getEmployeesForProfile(profileId);
    }

    public void assignCoordinator(int userId, int profileId) throws Exception {
        dataAccess.assignEmployees(userId, profileId);
    }

    public void removeCoordinator(int userId, int profileId) throws Exception {
        dataAccess.removeEmployees(userId, profileId);
    }

    public List<Profile> getProfilesForEmployee(int userId) throws Exception {
        return dataAccess.getProfilesForEmployee(userId);
    }
}