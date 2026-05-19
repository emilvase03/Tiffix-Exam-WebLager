package dk.easv.tiffixexamweblager.GUI.Controllers;

import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;
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
    private void onEmployees() {
        setActiveButton(btnEmployees);
        dashboard.loadView("views/EmployeesTab.fxml");
    }

    @FXML
    private void onProfiles() {
        setActiveButton(btnProfiles);
        dashboard.loadView("views/ProfilesTab.fxml");
    }

    @FXML
    private void onCustomers(ActionEvent event) {
        setActiveButton(btnCustomers);
        dashboard.loadView("views/CustomersTab.fxml");
    }

    @FXML
    private void onBoxes(ActionEvent actionEvent) {
        setActiveButton(btnBoxes);
        dashboard.loadView("views/BoxesTab.fxml");
    }

    @FXML
    private void onMetadata(ActionEvent event) {
        setActiveButton(btnMetadata);
        dashboard.loadView("views/MetadataTab.fxml");
    }

    @FXML
    private void onLogs(ActionEvent event) {
        setActiveButton(btnLogs);
        dashboard.loadView("views/LogsTab.fxml");
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

    @FXML
    public void onLogout(ActionEvent actionEvent) {
        boolean confirmed = AlertHelper.showConfirmation(
                "Log out",
                "Are you sure you want to log out?"
        );

        if (confirmed) {
            UserSession.getInstance().clear();
            ViewHandler.ADMIN_DASHBOARD.close();
            ViewHandler.ADMIN_DASHBOARD.reset();
            ViewHandler.LOGIN.show(false);
        }
    }
}