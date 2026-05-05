package dk.easv.tiffixexamweblager.GUI.Controllers;

import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.DocumentTileController;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.ScannedFileTileController;
import dk.easv.tiffixexamweblager.GUI.Models.DocumentModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;

import atlantafx.base.controls.ModalPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class EmployeeDashboardController {


    @FXML private ModalPane modalPane;
    @FXML private Label lblBoxID;
    @FXML private Label lblDocumentNr;
    @FXML private Label lblTotalDocInBox;
    @FXML private Label lblTotalFilesInDoc;
    @FXML private TilePane documentsTilePane;
    @FXML private TilePane filesTilePane;
    @FXML private BorderPane topOverview;

    private DocumentModel documentModel;


    @FXML
    private void initialize() {
        try {
            documentModel = new DocumentModel();
        } catch (Exception e) {
            AlertHelper.showError(
                    "Documents unavailable",
                    "The documents could not be loaded."
            );
        }
    }

    private void showChooseProfileModal() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/ChooseProfileView.fxml"));
            Parent modalContent = loader.load();

            ChooseProfileController controller = loader.getController();
            controller.init(modalPane, this::onSessionStarted);

            modalPane.show(modalContent);
        } catch (Exception e) {
            AlertHelper.showError(
                    "Unable to open profile selection",
                    "The profile selection could not be opened."
            );
        }
    }

    private void onSessionStarted() {
        try {
            Box box = UserSession.getInstance().getActiveBox();

            lblBoxID.setText(String.valueOf(box.getNumber()));

            var documents = documentModel.loadDocumentsForBox(box);
            lblTotalDocInBox.setText(String.valueOf(documents.size()));

            populateDocumentTilePane(documents);

            // reset file view
            filesTilePane.getChildren().clear();
            lblTotalFilesInDoc.setText("0");
            lblDocumentNr.setText("-");

        } catch (Exception e) {
            AlertHelper.showError(
                    "Load error",
                    "Could not load documents for selected box."
            );
        }
    }

    private void populateDocumentTilePane(Iterable<Document> documents) {
        documentsTilePane.getChildren().clear();

        for (Document doc : documents) {
            documentsTilePane.getChildren().add(createDocumentTile(doc));
        }
    }

    private Node createDocumentTile(Document doc) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/DocumentTileView.fxml"));
            Node tile = loader.load();

            DocumentTileController controller = loader.getController();
            controller.setDocument(doc);

            tile.setOnMouseClicked(e -> onDocumentSelected(doc));

            return tile;
        } catch (Exception e) {
            AlertHelper.showError(
                    "Error",
                    "One or more documents could not be shown."
            );
            return new VBox();
        }
    }

    private void onDocumentSelected(Document doc) {
        try {

            lblDocumentNr.setText(String.valueOf(doc.getSortOrder()));

            var files = documentModel.loadFilesForDocument(doc);

            lblTotalFilesInDoc.setText(String.valueOf(files.size()));
            populateFileTilePane(files);

        } catch (Exception e) {
            AlertHelper.showError(
                    "Error",
                    "Could not load files for the selected document."
            );
        }
    }

    private void populateFileTilePane(Iterable<ScannedFile> files) {
        filesTilePane.getChildren().clear();

        for (ScannedFile file : files) {
            filesTilePane.getChildren().add(createFileTile(file));
        }
    }

    private Node createFileTile(ScannedFile file) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/ScannedFileTileView.fxml"));
            Node tile = loader.load();

            ScannedFileTileController controller = loader.getController();
            controller.setFile(file);

            tile.setOnMouseClicked(e -> onFileSelected(file));

            return tile;
        } catch (Exception e) {
            AlertHelper.showError(
                    "Error",
                    "Some files could not be displayed right now"
            );
            return new VBox();
        }
    }

    private void onFileSelected(ScannedFile file) {
        topOverview.setVisible(false);
    }

    @FXML
    private void onBtnStartScanningSession(ActionEvent event) {
        showChooseProfileModal();
    }

    @FXML
    private void onLogout(ActionEvent event) {
        UserSession.getInstance().clear();
        ViewHandler.EMPLOYEE_DASHBOARD.close();
        ViewHandler.EMPLOYEE_DASHBOARD.reset();
        ViewHandler.LOGIN.show(false);
    }

    @FXML private void onBtnExport(ActionEvent e) {

    }
    @FXML private void onBtnCloseOverview(ActionEvent e) {

    }
    @FXML private void onBtnPreviousPage(ActionEvent e) {

    }
    @FXML private void onBtnRotate(ActionEvent e) {

    }
    @FXML private void onBtnNext(ActionEvent e) {

    }
}