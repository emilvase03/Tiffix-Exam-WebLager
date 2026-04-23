package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Role;
import dk.easv.tiffixexamweblager.BE.User;

// Java imports
import java.util.List;

public interface IUserDataAccess {

    List<User> getAllUsers() throws Exception;

    User createUser(User user) throws Exception;

    void updateUser(User user) throws Exception;

    void deleteUser(User user) throws Exception;

    boolean usernameExists(String username) throws Exception;

    User getUserByUsername(String username) throws Exception;

    List<User> getUsersByRole(Role role) throws Exception;
}
