package dk.easv.tiffixexamweblager.DAL;

import dk.easv.tiffixexamweblager.BE.UserProfile;

import java.util.List;

public interface IUserProfileDataAccess {
    List<UserProfile> getEmployeesForProfile(int profileId) throws Exception;
    void assignEmployees(int userId, int profileId) throws Exception;
    void removeEmployees(int userId, int profileId) throws Exception;
}
