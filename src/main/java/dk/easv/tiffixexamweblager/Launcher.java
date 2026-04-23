package dk.easv.tiffixexamweblager;

// Project imports
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;

// AtlantaFX imports
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;

// Java imports
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

public class Launcher extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        ViewHandler.ADMIN_DASHBOARD.show(false);

    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
