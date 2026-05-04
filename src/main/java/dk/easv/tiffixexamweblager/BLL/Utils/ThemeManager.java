package dk.easv.tiffixexamweblager.BLL.Utils;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.util.prefs.Preferences;

public class ThemeManager {

    private static ThemeManager instance;
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String THEME_KEY = "dm";

    private final BooleanProperty darkMode = new SimpleBooleanProperty(false);

    private ThemeManager() {
        darkMode.set(prefs.getBoolean(THEME_KEY, false));

        darkMode.addListener((obs, oldVal, isDark) -> {
            applyTheme(isDark);
            prefs.putBoolean(THEME_KEY, isDark);
        });

        applyTheme(darkMode.get());
    }

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    public void toggle() {
        darkMode.set(!darkMode.get());
    }

    public BooleanProperty darkModeProperty() {
        return darkMode;
    }

    public boolean isDark() {
        return darkMode.get();
    }

    private void applyTheme(boolean dark) {
        Application.setUserAgentStylesheet(
                dark ? new NordDark().getUserAgentStylesheet()
                        : new NordLight().getUserAgentStylesheet()
        );
    }
}