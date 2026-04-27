package dk.easv.tiffixexamweblager.GUI.Controllers;

import atlantafx.base.controls.ModalPane;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class AdminDashboardController {

    @FXML
    private StackPane root;

    @FXML
    private StackPane contentArea;
    @FXML
    private ModalPane modalPane;
    @FXML
    private Parent drawer;

    private static final double   SIDEBAR_WIDTH  = 250;
    private static final Duration SLIDE_DURATION = Duration.millis(150);
    private static final Duration FADE_DURATION  = Duration.millis(120);

    @FXML
    public void initialize() {
        loadSidebar();
        setupModalPane();
        loadInitialView();
    }
    private void loadSidebar() {
        try {
            URL sidebarUrl = getClass().getResource("/views/SideBar.fxml");
            if (sidebarUrl == null) {
                throw new IllegalStateException("SideBar.fxml not found in resources root");
            }

            FXMLLoader loader = new FXMLLoader(sidebarUrl);
            drawer = loader.load();

            SideBarController drawerController = loader.getController();
            drawerController.setDashboard(this);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load sidebar", e);
        }
    }
    private void setupModalPane() {
        modalPane = new ModalPane();
        modalPane.setAlignment(Pos.CENTER_LEFT);
        modalPane.setPersistent(false);
        modalPane.setViewOrder(-1);

        root.getChildren().add(modalPane);
        modalPane.hide();
    }
    private void loadInitialView() {
        loadView("views/EmployeesTab.fxml");
    }
    @FXML
    private void onBtnOpenSideBar() {
        if (modalPane == null || drawer == null) return;
        modalPane.show(drawer);

        // Slide in from left
        TranslateTransition slide = new TranslateTransition(SLIDE_DURATION, drawer);
        slide.setFromX(-SIDEBAR_WIDTH);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(FADE_DURATION, drawer);
        fade.setFromValue(0);
        fade.setToValue(1);

        new ParallelTransition(slide, fade).play();
    }

    public void closeSideBar() {
        if (modalPane == null || drawer == null || !modalPane.isDisplay()) return;

        TranslateTransition slide = new TranslateTransition(SLIDE_DURATION, drawer);
        slide.setFromX(0);
        slide.setToX(-SIDEBAR_WIDTH);
        slide.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fade = new FadeTransition(FADE_DURATION, drawer);
        fade.setFromValue(1);
        fade.setToValue(0);

        ParallelTransition out = new ParallelTransition(slide, fade);
        out.setOnFinished(e -> modalPane.hide()); // hide only after animation ends
        out.play();
    }

    public void loadView(String fxml) {
        try {
            URL viewUrl = getClass().getResource("/" + fxml);
            if (viewUrl == null) {
                throw new IllegalStateException(fxml + " not found in resources root");
            }
            FXMLLoader loader = new FXMLLoader(viewUrl);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            if (modalPane != null && modalPane.isDisplay()) {
                closeSideBar();
            }

        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load view.");
            e.printStackTrace();
        }
    }
}
