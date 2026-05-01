package dk.easv.tiffixexamweblager.GUI.Controllers;

import atlantafx.base.controls.ModalPane;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class EmployeeDashboardController {

    @FXML
    private StackPane root;
    @FXML
    private ModalPane modalPane;
    @FXML
    private Label lblBoxID;
    @FXML
    private Label lblDocumentNr;
    @FXML
    private Label lblTotalDocInBox;
    @FXML
    private BorderPane topOverview;
    @FXML
    private ImageView previewImageView;

    @FXML
    private void initialize() {

        Platform.runLater(this::showChooseProfileModal);
    }

    private void showChooseProfileModal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/ChooseProfileView.fxml")
            );
            Parent modalContent = loader.load();
            ChooseProfileController controller = loader.getController();
            controller.init(modalPane);
            modalPane.show(modalContent);

        } catch (Exception e) {

            AlertHelper.showError(
                    "Load error",
                    "Could not open profile selector"
            );
        }
    }
    //not nice
    @FXML
    public void onLogout(ActionEvent actionEvent) {
            ViewHandler.EMPLOYEE_DASHBOARD.close();
            ViewHandler.EMPLOYEE_DASHBOARD.reset();
            ViewHandler.LOGIN.show(false);
        }

    @FXML
    private void onBtnExport(ActionEvent actionEvent) {
    }

    @FXML
    private void onBtnCloseOverview(ActionEvent actionEvent) {
    }

    @FXML
    private void onBtnPreviousPage(ActionEvent actionEvent) {
    }

    @FXML
    private void onBtnRotate(ActionEvent actionEvent) {
    }

    @FXML
    private void onBtnNext(ActionEvent actionEvent) {
    }
}
