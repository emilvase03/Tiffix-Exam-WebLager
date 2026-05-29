package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BE.Role;
import dk.easv.tiffixexamweblager.BLL.Utils.Encrypter;
import dk.easv.tiffixexamweblager.BLL.Utils.LogAction;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.DAL.DAO.UserDAO;
import dk.easv.tiffixexamweblager.DAL.IUserDataAccess;

// Java imports
import java.util.List;

public class UserManager {

    private final IUserDataAccess dataAccess;
    private final LogManager      logManager;

    public UserManager() throws Exception {
        dataAccess = new UserDAO();
        logManager = new LogManager();
    }

    public List<User> getAllUsers() throws Exception {
        return dataAccess.getAll();
    }

    public List<User> getEmployees() throws Exception {
        return dataAccess.getUsersByRole(Role.EMPLOYEE);
    }

    public User loginUser(String username, String password) throws Exception {
        if (username == null || password == null)
            return null;

        User user = dataAccess.getUserByUsername(username);
        if (user == null)
            return null;

        Encrypter.verifyPassword(password, user.getPassword());

        log(user.getId(), user.getUsername() + ": logged in");

        return user;
    }

    public User createUser(String firstName, String lastName,
                           String username, String password, Role role) throws Exception {
        if (dataAccess.usernameExists(username))
            return null;

        User newUser = new User(-1, firstName, lastName, username,
                Encrypter.hashPassword(password), role);

        User created = dataAccess.create(newUser);
        log(LogAction.CREATE, "Created user: " + username + " (" + role + ")");
        return created;
    }

    public void updateUser(User user, String rawPassword) throws Exception {
        if (rawPassword != null && !rawPassword.isBlank())
            user.setPassword(Encrypter.hashPassword(rawPassword));

        dataAccess.update(user);
        log(LogAction.UPDATE, "Updated user: " + user.getUsername());
    }

    public void deleteUser(User user) throws Exception {
        dataAccess.delete(user);
        log(LogAction.DELETE, "Deleted user: " + user.getUsername());
    }

    public List<User> getAllIncludingSoftDeleted() throws Exception {
        return dataAccess.getAllIncludingSoftDeleted();
    }

    public boolean toggleSoftDelete(int id) throws Exception {
        boolean result = dataAccess.toggleSoftDelete(id);
        log(LogAction.UPDATE, "Toggled active status for user ID: " + id);
        return result;
    }

    // helpers

    /** post-login logger */
    private void log(LogAction action, String message) {
        try {
            User current = UserSession.getInstance().getCurrentUser();
            if (current != null) {
                logManager.log(current.getId(), action, current.getUsername() + ": " + message);
            }
        } catch (Exception ignored) {}
    }

    /** pre-login logger */
    private void log(int userId, String message) {
        try {
            logManager.log(userId, LogAction.LOGIN, message);
        } catch (Exception ignored) {}
    }

    /*public static void main(String[] args) {
        try {
            UserManager userManager = new UserManager();

            User admin = userManager.createUser("Test", "Admin", "a", "a", Role.ADMIN);
            System.out.println(admin != null ? "Admin created." : "Admin already exists.");

            User employee = userManager.createUser("Test", "Employee", "e", "e", Role.EMPLOYEE);
            System.out.println(employee != null ? "Employee created." : "Employee already exists.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}