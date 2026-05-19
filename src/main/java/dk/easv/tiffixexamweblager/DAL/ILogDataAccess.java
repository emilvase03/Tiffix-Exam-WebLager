package dk.easv.tiffixexamweblager.DAL;

import dk.easv.tiffixexamweblager.BE.LogEntry;

import java.util.List;

public interface ILogDataAccess {

    void insertLog(int userId, int typeId, String message) throws Exception;
    List<LogEntry> getAllLogEntries() throws Exception;
}