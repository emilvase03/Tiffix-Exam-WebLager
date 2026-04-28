package dk.easv.tiffixexamweblager.BLL;

import dk.easv.tiffixexamweblager.BE.UserProfile;
import dk.easv.tiffixexamweblager.DAL.DAO.UserProfileDAO;
import dk.easv.tiffixexamweblager.DAL.IUserProfileDataAccess;

import java.util.List;

public class UserProfileManager {

    private final IUserProfileDataAccess userEventDAO;

    public UserProfileManager() throws Exception {
        userEventDAO = new UserProfileDAO();
    }

    public List<UserProfile> getCoordinatorsForEvent(int profileId) throws Exception {
        return userEventDAO.getEmployeesForProfile(profileId);
    }

    public void assignCoordinator(int userId, int profileId) throws Exception {
        userEventDAO.assignEmployees(userId, profileId);
    }

    public void removeCoordinator(int userId, int profileId) throws Exception {
        userEventDAO.removeEmployees(userId, profileId);
    }
}
