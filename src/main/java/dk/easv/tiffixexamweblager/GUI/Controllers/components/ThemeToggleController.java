package dk.easv.tiffixexamweblager.GUI.Controllers.components;

import dk.easv.tiffixexamweblager.BLL.Utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.util.Duration;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;

public class ThemeToggleController {

    @FXML private Button themeBtn;
    @FXML private FontIcon themeIcon;

    @FXML
    public void initialize() {
        ThemeManager tm = ThemeManager.getInstance();

        themeBtn.getStyleClass().addAll("button-circle", "flat");
        themeIcon.setIconCode(tm.isDark() ? BootstrapIcons.MOON : BootstrapIcons.SUN);

        themeBtn.setOnAction(e -> {
            tm.toggle();
            animateIcon(tm.isDark());
        });
    }

    private void animateIcon(boolean isDark) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), themeIcon);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        RotateTransition rotateOut = new RotateTransition(Duration.millis(150), themeIcon);
        rotateOut.setByAngle(isDark ? 90 : -90);

        ParallelTransition out = new ParallelTransition(fadeOut, rotateOut);
        out.setOnFinished(e -> {
            themeIcon.setIconCode(isDark ? BootstrapIcons.MOON : BootstrapIcons.SUN);
            themeIcon.setRotate(isDark ? -90 : 90);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), themeIcon);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            RotateTransition rotateIn = new RotateTransition(Duration.millis(150), themeIcon);
            rotateIn.setToAngle(0);

            new ParallelTransition(fadeIn, rotateIn).play();
        });

        out.play();
    }
}
