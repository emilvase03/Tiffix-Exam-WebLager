package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.DocumentTileController;
import dk.easv.tiffixexamweblager.GUI.Models.DocumentModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;

// JavaFX imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;


import atlantafx.base.controls.ModalPane;

public class EmployeeDashboardController {

    @FXML private StackPane root;
    @FXML private ModalPane modalPane;
    @FXML private Label lblBoxID;
    @FXML private Label lblDocumentNr;
    @FXML private Label lblTotalDocInBox;
    @FXML private ImageView previewImageView;
    @FXML private TilePane documentsTilePane;

    private DocumentModel documentModel;

    @FXML
    private void initialize() {
        try {
            documentModel = new DocumentModel();
        } catch (Exception e) {
            AlertHelper.showError("Documents unavailable", "The documents could not be loaded  now.");
        }
    }

    private void showChooseProfileModal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/ChooseProfileView.fxml")
            );
            Parent modalContent = loader.load();
            ChooseProfileController controller = loader.getController();

            // Once the user has selected a profile and a box, the dashboard must update itself and show what is inside that box.
            controller.init(modalPane, this::onSessionStarted);

            modalPane.show(modalContent);
        } catch (Exception e) {
            AlertHelper.showError("Unable to open profile selection", "The profile selection could not be opened at this time.");
        }
    }

           //runs after the modal closes
    private void onSessionStarted() {
        try {
            Box box = UserSession.getInstance().getActiveBox();

            //  label
            lblBoxID.setText(String.valueOf(box.getNumber()));

            var documents = documentModel.loadDocumentsForBox(box);
            lblTotalDocInBox.setText(String.valueOf(documents.size()));

            populateTilePane(documents);
        } catch (Exception e) {
            AlertHelper.showError("Load error", "Could not load documents for selected box.");
        }
    }

   //take each document and cfreaate visual tile for each doc
    private void populateTilePane(Iterable<Document> documents) {
        documentsTilePane.getChildren().clear();

        for (Document doc : documents) {
            documentsTilePane.getChildren().add(createDocumentTile(doc));
        }
    }
    //creates a clickable  tile for one document
    private Node createDocumentTile(Document doc) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/DocumentTileView.fxml")
            );
            Node tile = loader.load();

            DocumentTileController controller = loader.getController();
            controller.setDocument(doc);

            tile.setOnMouseClicked(e -> onDocumentSelected(doc));

            return tile;
        } catch (Exception e) {
            AlertHelper.showError("Documents cannot be displayed", "One or more documents could not be shown.");
            return new VBox();
        }
    }


    private void onDocumentSelected(Document doc) {
        lblDocumentNr.setText(String.valueOf(doc.getSortOrder()));
    }

    @FXML
    public void onLogout(ActionEvent actionEvent) {
        UserSession.getInstance().clear();
        ViewHandler.EMPLOYEE_DASHBOARD.close();
        ViewHandler.EMPLOYEE_DASHBOARD.reset();
        ViewHandler.LOGIN.show(false);
    }

    @FXML private void onBtnExport(ActionEvent actionEvent) {

    }
    @FXML private void onBtnCloseOverview(ActionEvent actionEvent) {

    }
    @FXML private void onBtnPreviousPage(ActionEvent actionEvent) {

    }
    @FXML private void onBtnRotate(ActionEvent actionEvent) {

    }
    @FXML private void onBtnNext(ActionEvent actionEvent) {

    }

    @FXML
    private void onBtnStartScanningSession(ActionEvent actionEvent) {
        showChooseProfileModal();
    }
}