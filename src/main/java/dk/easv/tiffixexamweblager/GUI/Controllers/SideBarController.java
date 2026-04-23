package dk.easv.tiffixexamweblager.GUI.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SideBarController {

    @FXML
    private Button btnEmployees;
    @FXML
    private Button btnProfiles;

    private AdminDashboard dashboard;

    public void setDashboard(AdminDashboard dashboard) {

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

    // Visually highlights the selected nav button
    private void setActiveButton(Button active) {
        btnEmployees.getStyleClass().remove("sidebar-btn-active");
        btnProfiles.getStyleClass().remove("sidebar-btn-active");
        active.getStyleClass().add("sidebar-btn-active");
    }


    @FXML
    private void onBtnCloseSideBar(ActionEvent event) {
        dashboard.closeSideBar();
    }
}


