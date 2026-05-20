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
            vboxContainer.getChildren().add(new VBox(new Label("F1        = create/refresh button")));
            vboxContainer.getChildren().add(new VBox(new Label("ESC      = Log out")));

        } else  {
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + s                 = Start scanning session")));
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + f                 = Fetch new file")));
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + x                = Export box")));
            vboxContainer.getChildren().add(new VBox(new Label("ESC                      = Close preview of file")));
            vboxContainer.getChildren().add(new VBox(new Label("Backspace           = Rescan file in preview")));
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + r                 = Rotate file in preview")));
            vboxContainer.getChildren().add(new VBox(new Label("Arrow key, right  = Show next file in preview")));
            vboxContainer.getChildren().add(new VBox(new Label("Arrow key, left     = Show previous file in preview")));
            vboxContainer.getChildren().add(new VBox(new Label("Tab                      = Go to next document/file in tree structure")));
            vboxContainer.getChildren().add(new VBox(new Label("Tab + Shift          = Go to previous document/file in tree structure")));
            vboxContainer.getChildren().add(new VBox(new Label("Enter                   = Open document in tree structure")));
            vboxContainer.getChildren().add(new VBox(new Label("ctrl + l                 = Log out")));
        }
    }
}
