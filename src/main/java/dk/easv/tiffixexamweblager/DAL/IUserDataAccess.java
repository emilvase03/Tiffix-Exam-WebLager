package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Role;
import dk.easv.tiffixexamweblager.BE.User;

// Java imports
import java.util.List;

public interface IUserDataAccess extends ICRUDDataAccess<User> {
    public boolean usernameExists(String username) throws Exception;

    public User getUserByUsername(String username) throws Exception;

    public List<User> getUsersByRole(Role role) throws Exception;
}
