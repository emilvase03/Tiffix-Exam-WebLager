package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BLL.ProfileManager;

// Java imports
import dk.easv.tiffixexamweblager.BLL.UserProfileManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class ProfileModel {
    private ProfileManager manager;
    private ObservableList<Profile> allProfiles = FXCollections.observableArrayList();
    private final UserProfileManager userProfileManager;


    public ProfileModel() throws Exception {
        manager = new ProfileManager();
        userProfileManager = new UserProfileManager();
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

    public UserProfileManager getUserProfileManager() {
        return userProfileManager;
    }
}
