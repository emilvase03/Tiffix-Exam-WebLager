package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Java imports
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ShortcutCardController {
    @FXML private VBox vboxContainer;

    public void preloadWindow(boolean isAdmin) {
        vboxContainer.getChildren().clear();
        if (isAdmin) {
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + e = open employees tab")));
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + p = open profiles tab")));
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + c = open customers tab")));
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + b = open boxes tab")));
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + m = open metadata tab")));
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + l = open logs tab")));
            vboxContainer.getChildren().add(new VBox(new Label("F1       = create/refresh button")));
            vboxContainer.getChildren().add(new VBox(new Label("ESC     = Log out")));

        } else  {
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + c = open customers tab")));
        }
    }
}
