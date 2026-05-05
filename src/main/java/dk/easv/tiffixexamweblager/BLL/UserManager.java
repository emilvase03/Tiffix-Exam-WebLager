package dk.easv.tiffixexamweblager.BLL;

import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BE.Role;
import dk.easv.tiffixexamweblager.BLL.Utils.Encrypter;
import dk.easv.tiffixexamweblager.DAL.DAO.UserDAO;
import dk.easv.tiffixexamweblager.DAL.IUserDataAccess;

import java.util.List;

public class UserManager {

    private final IUserDataAccess dataAccess;

    public UserManager() throws Exception {
        dataAccess = new UserDAO();
    }

    public List<User> getAllUsers() throws Exception {
        return dataAccess.getAllUsers();
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

        return user;
    }

    public User createUser(String firstName,
                           String lastName,
                           String username,
                           String password,
                           Role role) throws Exception {

        if (dataAccess.usernameExists(username))
            return null;

        String hashedPassword = Encrypter.hashPassword(password);

        User newUser = new User(
                -1,
                firstName,
                lastName,
                username,
                hashedPassword,
                role
        );

        return dataAccess.createUser(newUser);
    }

    public void updateUser(User user, String rawPassword) throws Exception {
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(Encrypter.hashPassword(rawPassword));
        }
        dataAccess.updateUser(user);
    }


    public void deleteUser(User user) throws Exception {
        dataAccess.deleteUser(user);
    }

    public static void main(String[] args) {

        try {
            UserManager userManager = new UserManager();

            // Create Admin
            User admin = userManager.createUser(
                    "Test",
                    "Admin",
                    "a",
                    "a",
                    Role.ADMIN
            );

            if (admin != null)
                System.out.println("Admin created successfully!");
            else
                System.out.println("Admin already exists.");

            // Create Event user
            User eventUser = userManager.createUser(
                    "Test",
                    "Employee",
                    "e",
                    "e",
                    Role.EMPLOYEE
            );

            if (eventUser != null)
                System.out.println("Employee user created successfully!");
            else
                System.out.println("Employee user already exists.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
