package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;

// Java imports
import java.util.List;

public interface IProfileDataAccess {
    public List<Profile> getAllProfiles() throws Exception;

    public Profile createProfile(Profile newProfile) throws Exception;

    public void updateProfile(Profile profile) throws Exception;

    public void deleteProfile(Profile profile) throws Exception;
}
