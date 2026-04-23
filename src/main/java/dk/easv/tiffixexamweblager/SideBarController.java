package dk.easv.tiffixexamweblager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SideBarController {

    @FXML
    private Button btnUsers;
    @FXML
    private Button btnProfiles;

    private AdminDashboard dashboard;

    public void setDashboard(AdminDashboard dashboard) {
        this.dashboard = dashboard;
    }

    @FXML
    private void onUsers() {
        setActiveButton(btnUsers);
        dashboard.loadView("UsersTab.fxml");
    }

    @FXML
    private void onProfiles() {
        setActiveButton(btnProfiles);
        dashboard.loadView("ProfilesTab.fxml");
    }

    // Visually highlights the selected nav button
    private void setActiveButton(Button active) {
        btnUsers.getStyleClass().remove("sidebar-btn-active");
        btnProfiles.getStyleClass().remove("sidebar-btn-active");
        active.getStyleClass().add("sidebar-btn-active");
    }


    @FXML
    private void onBtnCloseSideBar(ActionEvent event) {
        dashboard.closeSideBar();
    }
}


