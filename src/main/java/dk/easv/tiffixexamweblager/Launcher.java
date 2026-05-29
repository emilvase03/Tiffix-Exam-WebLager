package dk.easv.tiffixexamweblager;

// Project imports
import dk.easv.tiffixexamweblager.BLL.Utils.ThemeManager;
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;

// Java imports
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

public class Launcher extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        ThemeManager.getInstance();
        ViewHandler.LOGIN.show(false);

    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
