package dk.easv.tiffixexamweblager.BE;

public class UserProfile {
    private int id = -1;
    private int userId = -1;
    private int profileId = -1;

    public UserProfile(int id, int userId, int profileId) {
        setId(id);
        setUserId(userId);
        setProfileId(profileId);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id != -1)
            this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        if (userId != -1)
            this.userId = userId;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        if (profileId != -1)
            this.profileId = profileId;
    }
}

