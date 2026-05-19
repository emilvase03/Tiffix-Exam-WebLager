package dk.easv.tiffixexamweblager.GUI.Controllers;

import dk.easv.tiffixexamweblager.BE.LogEntry;
import dk.easv.tiffixexamweblager.GUI.Models.LogModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;

public class LogsTabController {

    @FXML private TableView<LogEntry> tblLogs;
    @FXML private TableColumn<LogEntry, String> colTime;
    @FXML private TableColumn<LogEntry, String> colUser;
    @FXML private TableColumn<LogEntry, String> colAction;
    @FXML private TableColumn<LogEntry, String> colMessage;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private LogModel logModel;

    @FXML
    public void initialize() {
        try {
            logModel = new LogModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to initialize log model.");
            return;
        }
        colTime   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCreatedAt().format(FORMATTER)));
        colUser   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colAction .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));
        colMessage.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMessage()));

        tblLogs.setItems(logModel.getLogs());
        loadLogs();
    }

    @FXML
    private void handleRefresh() {
        loadLogs();
    }

    private void loadLogs() {
        try {
            logModel.loadLogs();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load logs.");
        }
    }
}