package dk.easv.tiffixexamweblager.GUI.Utils;

// Java imports
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShortcutRegistry {
    private final Map<KeyCombination, Runnable> shortcuts = new LinkedHashMap<>();
    private Scene scene;
    private EventHandler<KeyEvent> handler;
    private Runnable primaryAction;

    public ShortcutRegistry register(KeyCombination combo, Runnable action) {
        shortcuts.put(combo, action);
        return this;
    }

    public void setPrimaryAction(Runnable action) {
        this.primaryAction = action;
    }

    public void attach(Scene scene) {
        this.scene = scene;
        handler = event -> {
            for (Map.Entry<KeyCombination, Runnable> entry : shortcuts.entrySet()) {
                if (entry.getKey().match(event)) {
                    entry.getValue().run();
                    event.consume();
                    return;
                }
            }
            if (event.getCode() == KeyCode.F1 && primaryAction != null) {
                primaryAction.run();
                event.consume();
            }
        };
        scene.addEventFilter(KeyEvent.KEY_PRESSED, handler);
    }

    public void detach() {
        if (scene != null && handler != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, handler);
            handler = null;
        }
    }
}
