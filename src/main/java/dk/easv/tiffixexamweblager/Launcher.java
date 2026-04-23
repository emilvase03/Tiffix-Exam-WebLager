package dk.easv.tiffixexamweblager;

// AtlantaFX imports
import atlantafx.base.theme.NordDark;

// Java imports
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Launcher extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/AdminDashboard.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Tiffix");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
