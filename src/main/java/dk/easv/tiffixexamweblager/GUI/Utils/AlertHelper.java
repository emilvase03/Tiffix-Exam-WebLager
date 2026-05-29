package dk.easv.tiffixexamweblager.GUI.Utils;

// Java imports
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AlertHelper {

    // private constructor to prevent instantiation
    private AlertHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // shows a generic alert
    public static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // shows an error alert
    public static void showError(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    // shows an information alert
    public static void showInformation(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    // shows a warning alert
    public static void showWarning(String title, String message) {
        showAlert(title, message, Alert.AlertType.WARNING);
    }

    // shows a confirmation dialog and returns true if user clicks ok
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // shows a confirmation dialog with custom button text
    public static boolean showConfirmation(String title, String message, String okText, String cancelText) {
        ButtonType okButton = new ButtonType(okText);
        ButtonType cancelButton = new ButtonType(cancelText);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(okButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == okButton;
    }

    // shows an exception alert with exandable stack trace
    public static void showException(String title, String message, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(ex.getMessage());

        // create expandable exception details
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        javafx.scene.layout.GridPane expContent = new javafx.scene.layout.GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new javafx.scene.control.Label("Stack trace:"), 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }
}

