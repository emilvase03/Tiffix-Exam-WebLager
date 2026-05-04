package dk.easv.tiffixexamweblager.BLL.Utils;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.User;

import java.util.Collections;
import java.util.List;

public class UserSession {

    private static UserSession instance;
    private User currentUser;
    private List<Profile> activeProfiles = Collections.emptyList();
    private Box activeBox;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null. Use clear() to log out.");
        }
        this.currentUser = user;
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("No user is currently logged in.");
        }
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void setActiveProfiles(List<Profile> profiles) {
        this.activeProfiles = profiles != null ? List.copyOf(profiles) : Collections.emptyList();
    }
    public List<Profile> getActiveProfiles() {
        return activeProfiles;
    }

    public void setActiveBox(Box box) {
        this.activeBox = box;
    }

    public Box getActiveBox() {
        return activeBox;
    }

    public boolean hasActiveBox() {
        return activeBox != null;
    }

    public void clear() {
        currentUser = null;
        activeProfiles = Collections.emptyList();
    }
}