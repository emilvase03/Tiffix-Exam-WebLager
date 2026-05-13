package dk.easv.tiffixexamweblager.GUI.Controllers.components;

import dk.easv.tiffixexamweblager.BLL.Utils.ThemeManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.util.Duration;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;

public class ThemeToggleController {

    @FXML private Button   themeBtn;
    @FXML private FontIcon themeIcon;

    @FXML
    public void initialize() {
        ThemeManager tm = ThemeManager.getInstance();
        themeBtn.getStyleClass().addAll("button-circle", "flat");
        themeIcon.setIconCode(tm.isDark() ? BootstrapIcons.MOON : BootstrapIcons.SUN);
        themeBtn.setOnAction(e -> animateReveal());
    }

    private void animateReveal() {
        Parent root = themeBtn.getScene().getRoot();
        themeBtn.setDisable(true);

        // Fade out the entire scene
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), root);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {

            // Switch theme while fully invisible — layout shift is hidden
            ThemeManager.getInstance().toggle();
            animateIcon(ThemeManager.getInstance().isDark());

            // Fade back in with new theme
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), root);
            fadeIn.setToValue(1.0);
            fadeIn.setOnFinished(ev -> themeBtn.setDisable(false));
            fadeIn.play();
        });

        fadeOut.play();
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