package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BLL.ProfileManager;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProfileModel {
    private ProfileManager manager;
    private ObservableList<Profile> allProfiles = FXCollections.observableArrayList();

    public ProfileModel() throws Exception {
        manager = new ProfileManager();
    }

    public ObservableList<Profile> getAllProfiles() throws Exception {
        allProfiles.setAll(manager.getAllProfiles());
        return allProfiles;
    };

    public Profile createProfile(Profile newProfile) throws Exception {
        return manager.createProfile(newProfile);
    };

    public void updateProfile(Profile profile) throws Exception {
        manager.updateProfile(profile);
    };

    public void deleteProfile(Profile profile) throws Exception {
        manager.deleteProfile(profile);
    };
}
