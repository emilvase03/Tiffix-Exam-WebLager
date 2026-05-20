package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;

// Java imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SideBarController {
    @FXML private Button btnEmployees;
    @FXML private Button btnProfiles;
    @FXML private Button btnCustomers;
    @FXML private Button btnBoxes;
    @FXML private Button btnMetadata;
    @FXML private Button btnLogs;

    private AdminDashboardController dashboard;

    public void setDashboard(AdminDashboardController dashboard) {
        this.dashboard = dashboard;
    }

    @FXML
    protected void onEmployees() {
        setActiveButton(btnEmployees);
        dashboard.loadView("views/EmployeesTab.fxml");
    }

    @FXML
    protected void onProfiles() {
        setActiveButton(btnProfiles);
        dashboard.loadView("views/ProfilesTab.fxml");
    }

    @FXML
    protected void onCustomers() {
        setActiveButton(btnCustomers);
        dashboard.loadView("views/CustomersTab.fxml");
    }

    @FXML
    protected void onBoxes() {
        setActiveButton(btnBoxes);
        dashboard.loadView("views/BoxesTab.fxml");
    }

    @FXML
    protected void onMetadata() {
        setActiveButton(btnMetadata);
        dashboard.loadView("views/MetadataTab.fxml");
    }

    @FXML
    protected void onLogs() {
        setActiveButton(btnLogs);
        dashboard.loadView("views/LogsTab.fxml");
    }

    protected void logout() {
        boolean confirmed = AlertHelper.showConfirmation("Log out", "Are you sure you want to log out?");
        if (confirmed) {
            UserSession.getInstance().clear();
            ViewHandler.ADMIN_DASHBOARD.close();
            ViewHandler.ADMIN_DASHBOARD.reset();
            ViewHandler.LOGIN.show(false);
        }
    }

    @FXML
    private void onLogout(ActionEvent event) {
        logout();
    }

    private void setActiveButton(Button active) {
        btnEmployees.getStyleClass().remove("sidebar-btn-active");
        btnProfiles.getStyleClass().remove("sidebar-btn-active");
        btnCustomers.getStyleClass().remove("sidebar-btn-active");
        btnBoxes.getStyleClass().remove("sidebar-btn-active");
        btnMetadata.getStyleClass().remove("sidebar-btn-active");
        btnLogs.getStyleClass().remove("sidebar-btn-active");
        active.getStyleClass().add("sidebar-btn-active");
    }

    @FXML
    private void onBtnCloseSideBar(ActionEvent event) {
        dashboard.closeSideBar();
    }
}