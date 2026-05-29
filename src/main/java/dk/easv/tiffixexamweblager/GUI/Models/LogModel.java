package dk.easv.tiffixexamweblager.GUI.Models;

// Project imports
import dk.easv.tiffixexamweblager.BE.LogEntry;
import dk.easv.tiffixexamweblager.BLL.LogManager;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LogModel {

    private final LogManager               logManager;
    private final ObservableList<LogEntry> logs = FXCollections.observableArrayList();

    public LogModel() throws Exception {
        logManager = new LogManager();
    }

    public ObservableList<LogEntry> getLogs() {
        return logs;
    }

    public void loadLogs() throws Exception {
        logs.setAll(logManager.getAllLogEntries());
    }
}