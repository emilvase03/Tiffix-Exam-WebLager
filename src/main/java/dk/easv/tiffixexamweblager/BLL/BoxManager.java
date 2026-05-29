package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BLL.Utils.LogAction;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.DAL.DAO.BoxDAO;
import dk.easv.tiffixexamweblager.DAL.IBoxDataAccess;

// Java imports
import java.util.List;

public class BoxManager {

    private final IBoxDataAccess dataAccess;
    private final LogManager     logManager;

    public BoxManager() throws Exception {
        dataAccess = new BoxDAO();
        logManager = new LogManager();
    }

    public List<Box> getAllBoxes() throws Exception {
        return dataAccess.getAll();
    }

    public Box createBox(Box box) throws Exception {
        Box created = dataAccess.create(box);
        log(LogAction.CREATE, "Created box: " + box.getTitle());
        return created;
    }

    public void deleteBox(Box box) throws Exception {
        dataAccess.delete(box);
        log(LogAction.DELETE, "Deleted box: " + box.getTitle());
    }

    public List<Box> getAllIncludingSoftDeleted() throws Exception {
        return dataAccess.getAllIncludingSoftDeleted();
    }

    public boolean toggleSoftDelete(int id) throws Exception {
        boolean result = dataAccess.toggleSoftDelete(id);
        log(LogAction.UPDATE, "Toggled active status for box ID: " + id);
        return result;
    }

    private void log(LogAction action, String message) {
        try {
            User current = UserSession.getInstance().getCurrentUser();
            if (current != null) {
                logManager.log(
                        current.getId(),
                        action,
                        current.getUsername() + ": " + message
                );
            }
        } catch (Exception ignored) {}
    }
}