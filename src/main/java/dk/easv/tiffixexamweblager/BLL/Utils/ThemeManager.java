package dk.easv.tiffixexamweblager.BLL.Utils;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class ThemeManager {

    private static final String DARK_CLASS = "nord-dark";

    private static ThemeManager instance;
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String THEME_KEY  = "dm";

    private final List<Scene>     scenes   = new ArrayList<>();
    private final BooleanProperty darkMode = new SimpleBooleanProperty(false);

    private ThemeManager() {
        darkMode.set(prefs.getBoolean(THEME_KEY, false));

        darkMode.addListener((obs, oldVal, isDark) -> {
            applyTheme(isDark);
            applyClassToAll(isDark);
            prefs.putBoolean(THEME_KEY, isDark);
        });

        applyTheme(darkMode.get());
        applyClassToAll(darkMode.get());

        Window.getWindows().addListener((ListChangeListener<Window>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(window -> {
                        // Apply to the scene that's already set (if any)
                        if (window.getScene() != null) {
                            applyClassTo(window.getScene(), darkMode.get());
                        }
                        // Also watch for scene changes on this window
                        // (scene may be set slightly after the window is added)
                        window.sceneProperty().addListener((sObs, oldScene, newScene) -> {
                            if (newScene != null) {
                                applyClassTo(newScene, darkMode.get());
                            }
                        });
                    });
                }
            }
        });
    }

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    public void toggle() {
        darkMode.set(!darkMode.get());
    }

    public BooleanProperty darkModeProperty() {
        return darkMode; }

    public boolean isDark()  {
        return darkMode.get(); }


    private void applyTheme(boolean dark) {
        Application.setUserAgentStylesheet(
                dark ? new NordDark().getUserAgentStylesheet()
                        : new NordLight().getUserAgentStylesheet());
    }

    private void applyClassToAll(boolean dark) {
        Window.getWindows().forEach(w -> {
            if (w.getScene() != null) applyClassTo(w.getScene(), dark);
        });
        scenes.forEach(s -> applyClassTo(s, dark));
    }

    private void applyClassTo(Scene scene, boolean dark) {
        if (scene == null || scene.getRoot() == null) return;
        var classes = scene.getRoot().getStyleClass();
        if (dark) {
            if (!classes.contains(DARK_CLASS)) classes.add(DARK_CLASS);
        } else {
            classes.remove(DARK_CLASS);
        }
    }
}