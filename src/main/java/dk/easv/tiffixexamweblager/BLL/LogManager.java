package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.LogEntry;
import dk.easv.tiffixexamweblager.BLL.Utils.LogAction;
import dk.easv.tiffixexamweblager.DAL.DAO.LogDAO;
import dk.easv.tiffixexamweblager.DAL.ILogDataAccess;

// Java imports
import java.util.List;

public class LogManager {

    private final ILogDataAccess dataAccess;

    public LogManager() throws Exception {
        dataAccess = new LogDAO();
    }

    public void log(int userId, LogAction action, String message) throws Exception {
        dataAccess.insertLog(userId, action.getId(), message);
    }

    public List<LogEntry> getAllLogEntries() throws Exception {
        return dataAccess.getAllLogEntries();
    }
}