package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.LogEntry;

// Java imports
import java.util.List;

public interface ILogDataAccess {

    void insertLog(int userId, int typeId, String message) throws Exception;
    List<LogEntry> getAllLogEntries() throws Exception;
}