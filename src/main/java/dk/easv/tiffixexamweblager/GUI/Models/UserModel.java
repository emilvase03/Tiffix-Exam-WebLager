package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BE.Role;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.BLL.UserManager;
import dk.easv.tiffixexamweblager.GUI.Utils.BackgroundExecutor;

// Java imports
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserModel {
    private final UserManager userManager;

    private final ObjectProperty<User> loggedInUser = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty loginFailed = new SimpleBooleanProperty(false);
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final ObservableList<User> employees = FXCollections.observableArrayList();


    public UserModel() {
        try {
            userManager = new UserManager();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize UserManager", e);
        }
    }

    public void loginUser(String username, String password) {
        loginFailed.set(false);

        BackgroundExecutor.execute(
                () -> userManager.loginUser(username, password),
                user -> {
                    if (user == null) {
                        loginFailed.set(true);
                    } else {
                        UserSession.getInstance().setCurrentUser(user);
                        loggedInUser.set(user);
                    }
                },
                e -> loginFailed.set(true),
                loading::set
        );
    }

    public void loadAllUsers() {
        BackgroundExecutor.execute(
                () -> userManager.getAllUsers(),
                users::setAll,
                e -> { throw new RuntimeException("Failed to load users", e); },
                loading::set
        );
    }

    public void loadEmployees() {
        loadEmployees(null);
    }

    public void loadEmployees(Runnable onLoaded) {
        BackgroundExecutor.execute(
                () -> userManager.getEmployees(),
                result -> {
                    employees.setAll(result);
                    if (onLoaded != null) onLoaded.run();
                },
                e -> { throw new RuntimeException("Failed to load coordinators", e); },
                loading::set
        );
    }

    public void createUser(String firstName,
                           String lastName,
                           String username,
                           String password,
                           Role role) {
        BackgroundExecutor.execute(
                () -> userManager.createUser(firstName, lastName, username, password, role),

                created -> {
                    if (created != null) {
                        users.add(created);
                        if (created.getRole() == Role.EMPLOYEE) {
                            employees.add(created);
                        }
                    }
                },

                e -> { throw new RuntimeException("Failed to create an employee", e); },
                loading::set
        );
    }

    public void updateUser(User user, String rawPassword) {
        BackgroundExecutor.execute(
                () -> {
                    userManager.updateUser(user, rawPassword);
                    return null;
                },

                result -> {
                    int userIndex = findUserIndex(users, user.getId());
                    if (userIndex >= 0) users.set(userIndex, user);

                    int empIndex = findUserIndex(employees, user.getId());
                    if (user.getRole() == Role.EMPLOYEE) {
                        if (empIndex >= 0) employees.set(empIndex, user);
                        else employees.add(user);
                    } else {
                        if (empIndex >= 0) employees.remove(empIndex);
                    }
                },

                e -> { throw new RuntimeException("Failed to update user", e); },
                loading::set
        );
    }

    private int findUserIndex(ObservableList<User> list, int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) return i;
        }
        return -1;
    }


    public void deleteUser(User user) {
        BackgroundExecutor.execute(
                () -> { userManager.deleteUser(user); return null; },
                result -> {
                    users.remove(user);
                    employees.remove(user);
                },
                e -> { throw new RuntimeException("Failed to delete user", e); },
                loading::set
        );
    }

    public ObservableList<User> getEmployees ()          { return employees; }
    public ObservableList<User> getUsers()                  { return users; }
    public ObjectProperty<User> loggedInUserProperty()      { return loggedInUser; }
    public BooleanProperty loadingProperty()                { return loading; }
    public BooleanProperty loginFailedProperty()            { return loginFailed; }
}
