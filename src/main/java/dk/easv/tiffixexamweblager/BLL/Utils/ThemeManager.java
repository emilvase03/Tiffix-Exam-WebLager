package dk.easv.tiffixexamweblager.BLL.Utils;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ThemeManager {

    private static ThemeManager instance;
    private final BooleanProperty darkMode = new SimpleBooleanProperty(false);

    private ThemeManager() {
        darkMode.addListener((obs, oldVal, isDark) -> applyTheme(isDark));
        applyTheme(false); // start with NordLight
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

    private void applyTheme(boolean dark) {
        Application.setUserAgentStylesheet(
                dark ? new NordDark().getUserAgentStylesheet()
                        : new NordLight().getUserAgentStylesheet()
        );
    }

    public boolean isDark() {
        return darkMode.get();
    }
}
