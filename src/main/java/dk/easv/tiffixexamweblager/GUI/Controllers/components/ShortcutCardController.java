package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Java imports
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ShortcutCardController {
    @FXML private VBox vboxContainer;

    private static final String[][] ADMIN_SHORTCUTS = {
            {"ctrl + e", "Open employees tab"},
            {"ctrl + p", "Open profiles tab"},
            {"ctrl + c", "Open customers tab"},
            {"ctrl + b", "Open boxes tab"},
            {"ctrl + m", "Open metadata tab"},
            {"ctrl + l", "Open logs tab"},
            {"F1",       "Create / refresh"},
            {"ESC",      "Log out"},
    };

    private static final String[][] USER_SHORTCUTS = {
            {"ctrl + s",         "Start scanning session"},
            {"ctrl + f",         "Fetch new file"},
            {"ctrl + x",         "Export box"},
            {"ESC",              "Close file preview"},
            {"Backspace",        "Rescan file in preview"},
            {"ctrl + r",         "Rotate file in preview"},
            {"→  Arrow key",     "Show next file in preview"},
            {"←  Arrow key",     "Show previous file in preview"},
            {"Tab",              "Next document in tree"},
            {"Tab + Shift",      "Previous document in tree"},
            {"Enter",            "Open document in tree"},
            {"ctrl + l",         "Log out"},
    };

    public void preloadWindow(boolean isAdmin) {
        vboxContainer.getChildren().clear();
        vboxContainer.getChildren().add(
                buildGrid(isAdmin ? ADMIN_SHORTCUTS : USER_SHORTCUTS)
        );
    }

    private GridPane buildGrid(String[][] shortcuts) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(4);
        grid.setPadding(new Insets(8));

        ColumnConstraints keyCol = new ColumnConstraints();
        keyCol.setHalignment(HPos.RIGHT);

        ColumnConstraints sepCol = new ColumnConstraints();
        sepCol.setHalignment(HPos.CENTER);

        ColumnConstraints descCol = new ColumnConstraints();

        grid.getColumnConstraints().addAll(keyCol, sepCol, descCol);

        for (int i = 0; i < shortcuts.length; i++) {
            grid.add(new Label(shortcuts[i][0]), 0, i);
            grid.add(new Label("="),             1, i);
            grid.add(new Label(shortcuts[i][1]), 2, i);
        }

        return grid;
    }
}