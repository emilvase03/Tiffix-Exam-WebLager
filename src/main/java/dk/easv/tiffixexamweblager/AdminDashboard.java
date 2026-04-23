package dk.easv.tiffixexamweblager;

import atlantafx.base.controls.ModalPane;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class AdminDashboard {

    // Root container for the entire dashboard view
    @FXML
    private StackPane root;

    // Area where main content  tabs are loaded
    @FXML
    private StackPane contentArea;

    // Sidebar
    private ModalPane modalPane;

    // Sidebar UI loaded from SideBar.fxml
    private Parent drawer;


    @FXML
    public void initialize() {
        try {
            //  Load sidebar
            URL sidebarUrl = getClass().getResource("/SideBar.fxml");
            if (sidebarUrl == null) {
                throw new IllegalStateException("SideBar.fxml not found in resources root");
            }

            FXMLLoader loader = new FXMLLoader(sidebarUrl);
            drawer = loader.load();

            // Give sidebar controller access the dashboard...used for a button later
            SideBarController drawerController = loader.getController();
            drawerController.setDashboard(this);


            modalPane = new ModalPane();
            modalPane.setAlignment(Pos.CENTER_LEFT); // Drawer opens from the left
            modalPane.setPersistent(false);          // Prevent click-outside closing
            modalPane.setMouseTransparent(false);    // Block background interaction
            modalPane.setViewOrder(-1);               // Ensure it stays on top
            modalPane.setFocusTraversable(true);      //??

            // Add sidebar above
            root.getChildren().add(modalPane);
            modalPane.hide(); // Sidebar hidden by default

            // tabs
            loadView("UsersTab.fxml");
            loadView("ProfilesTab.fxml");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBtnOpenSideBar() {
        if (modalPane != null && drawer != null) {
            modalPane.show(drawer);
        }
    }

    public void closeSideBar() {
        if (modalPane != null) {
            modalPane.hide();
        }
    }

    public void loadView(String fxml) {
        try {
            URL viewUrl = getClass().getResource("/" + fxml);
            if (viewUrl == null) {

                //!!!
                throw new IllegalStateException(fxml + " not found in resources root");
            }

            FXMLLoader loader = new FXMLLoader(viewUrl);
            Parent view = loader.load();

            // Replace existing content with the new view
            contentArea.getChildren().setAll(view);


            if (modalPane != null) {
                modalPane.hide();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}