package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.UserProfile;

// Java imports
import java.util.List;

public interface IUserProfileDataAccess {
    public List<UserProfile> getEmployeesForProfile(int profileId) throws Exception;

    public List<Profile> getProfilesForEmployee(int userId) throws Exception;

    public void assignEmployees(int userId, int profileId) throws Exception;

    public void removeEmployees(int userId, int profileId) throws Exception;
}
