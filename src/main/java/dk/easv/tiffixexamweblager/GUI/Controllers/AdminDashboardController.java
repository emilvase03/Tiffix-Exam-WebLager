package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.GUI.Controllers.components.ShortcutCardController;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import dk.easv.tiffixexamweblager.GUI.Utils.ShortcutRegistry;

// AtlantaFX imports
import atlantafx.base.controls.ModalPane;

// Java imports
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

public class AdminDashboardController {
    @FXML private StackPane root;
    @FXML private StackPane contentArea;
    @FXML private ModalPane modalPane;
    @FXML private Parent drawer;
    @FXML private VBox shortcutCardOverlay;
    @FXML private ShortcutCardController shortcutOverlayController;

    private final ShortcutRegistry shortcutRegistry = new ShortcutRegistry();
    private SideBarController sideBarController;

    private static final double   SIDEBAR_WIDTH  = 250;
    private static final Duration SLIDE_DURATION = Duration.millis(150);
    private static final Duration FADE_DURATION  = Duration.millis(120);


    @FXML
    public void initialize() {
        shortcutCardOverlay.prefWidthProperty().bind(root.widthProperty());
        shortcutCardOverlay.prefHeightProperty().bind(root.heightProperty());

        loadSidebar();
        setupModalPane();
        loadInitialView();

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null)
                shortcutRegistry.detach();
            if (newScene != null)
                registerShortcuts(newScene);
        });
    }

    private void registerShortcuts(Scene scene) {
        shortcutRegistry
                .register(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), sideBarController::onEmployees)
                .register(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN), sideBarController::onProfiles)
                .register(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN), sideBarController::onCustomers)
                .register(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN), sideBarController::onBoxes)
                .register(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN), sideBarController::onMetadata)
                .register(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN), sideBarController::onLogs)
                .register(new KeyCodeCombination(KeyCode.ESCAPE, KeyCombination.SHIFT_ANY), sideBarController::logout)
                .register(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN), () -> {
                    if (shortcutCardOverlay.isVisible())
                        hideOverlay();
                    else
                        showOverlay();
                })
                .attach(scene);
    }

    private void loadSidebar() {
        try {
            URL sidebarUrl = getClass().getResource("/views/SideBar.fxml");
            if (sidebarUrl == null) {
                throw new IllegalStateException("SideBar.fxml not found in resources root");
            }

            FXMLLoader loader = new FXMLLoader(sidebarUrl);
            drawer = loader.load();

            sideBarController = loader.getController();
            sideBarController.setDashboard(this);

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

            updatePrimaryAction(loader.getController());

            if (modalPane != null && modalPane.isDisplay()) {
                closeSideBar();
            }

        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load view.");
        }
    }

    private void updatePrimaryAction(Object controller) {
        if (controller instanceof LogsTabController c) {
            shortcutRegistry.setPrimaryAction(c::handleRefresh);
            return;
        }

        if (controller instanceof MetadataTabController) {
            shortcutRegistry.setPrimaryAction(null);
            return;
        }

        try {
            Method method = controller.getClass().getDeclaredMethod("handleCreate");
            method.setAccessible(true);
            shortcutRegistry.setPrimaryAction(() -> {
                try {
                    method.invoke(controller);
                } catch (Exception e) {
                    AlertHelper.showError("Error", "Failed to setup keyboard shortcut for create button");
                }
            });
        } catch (NoSuchMethodException e) {
            shortcutRegistry.setPrimaryAction(null);
        }
    }


    @FXML
    private void onMouseEnter(MouseEvent event) {
        showOverlay();
    }

    @FXML
    private void onMouseExit(MouseEvent event) {
        hideOverlay();
    }

    private void showOverlay() {
        shortcutOverlayController.preloadWindow(false);
        shortcutCardOverlay.setVisible(true);
    }

    private void hideOverlay() {
        shortcutCardOverlay.setVisible(false);
    }
}
