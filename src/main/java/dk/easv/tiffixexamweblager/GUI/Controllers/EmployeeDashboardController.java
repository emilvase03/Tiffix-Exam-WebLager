package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.DocumentTileController;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.ScannedFileTileController;
import dk.easv.tiffixexamweblager.GUI.Models.DocumentModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;

// JavaFX imports
import atlantafx.base.controls.ModalPane;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

// Java imports
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDashboardController {

    @FXML private StackPane root;
    @FXML private ModalPane modalPane;
    @FXML private Label lblBoxID;
    @FXML private Label lblDocumentNr;
    @FXML private Label lblTotalDocInBox;
    @FXML private Label lblTotalFilesInDoc;
    @FXML private BorderPane topOverview;
    @FXML private ImageView previewImageView;
    @FXML private TilePane documentsTilePane;
    @FXML private TilePane filesTilePane;

    private DocumentModel documentModel;

    private List<ScannedFile> currentFiles = new ArrayList<>();
    private int previewIndex = 0;

    private int previewRotation = 0;
    @FXML
    private Label lblTotalFilesText;
    @FXML
    private Label lblTotalDocText;

    @FXML
    private void initialize() {
        try {
            documentModel = new DocumentModel();
        } catch (Exception e) {
            AlertHelper.showError("Documents unavailable",
                    "The documents could not be loaded now.");
        }
        setTotalsVisible(false);
    }
    private void setTotalsVisible(boolean visible) {
        lblTotalDocText.setVisible(visible);
        lblTotalDocText.setManaged(visible);

        lblTotalDocInBox.setVisible(visible);
        lblTotalDocInBox.setManaged(visible);

        lblTotalFilesText.setVisible(visible);
        lblTotalFilesText.setManaged(visible);

        lblTotalFilesInDoc.setVisible(visible);
        lblTotalFilesInDoc.setManaged(visible);
    }



    private void showChooseProfileModal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/ChooseProfileView.fxml")
            );
            Parent modalContent = loader.load();
            ChooseProfileController controller = loader.getController();
            controller.init(modalPane, this::onSessionStarted);
            modalPane.show(modalContent);
        } catch (Exception e) {
            AlertHelper.showError("Unable to open profile selection",
                    "The profile selection could not be opened at this time.");
        }
    }

    private void onSessionStarted() {
        try {
            Box box = UserSession.getInstance().getActiveBox();
            lblBoxID.setText(String.valueOf(box.getNumber()));

            var documents = documentModel.loadDocumentsForBox(box);
            lblTotalDocInBox.setText(String.valueOf(documents.size()));

            populateDocumentTilePane(documents);

            filesTilePane.getChildren().clear();
            lblTotalFilesInDoc.setText("0");
            topOverview.setVisible(false);

            setTotalsVisible(true);

        } catch (Exception e) {
            AlertHelper.showError("Load error",
                    "Could not load documents for selected box.");
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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/DocumentTileView.fxml")
            );
            Node tile = loader.load();
            DocumentTileController controller = loader.getController();
            controller.setDocument(doc);
            tile.setOnMouseClicked(e -> onDocumentSelected(doc));
            return tile;
        } catch (Exception e) {
            AlertHelper.showError("Display error",
                    "One or more document tiles could not be shown.");
            return new VBox();
        }
    }

    private void onDocumentSelected(Document doc) {
        try {
            lblDocumentNr.setText(String.valueOf(doc.getSortOrder()));

            var files = documentModel.loadFilesForDocument(doc);
            lblTotalFilesInDoc.setText(String.valueOf(files.size()));

            currentFiles = new ArrayList<>(files);

            populateFileTilePane(currentFiles);
            topOverview.setVisible(false);
        } catch (Exception e) {
            AlertHelper.showError("Load error",
                    "Could not load files for the selected document.");
        }
    }

    private void populateFileTilePane(List<ScannedFile> files) {
        filesTilePane.getChildren().clear();
        for (ScannedFile file : files) {
            filesTilePane.getChildren().add(createFileTile(file));
        }
    }

    private Node createFileTile(ScannedFile file) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/ScannedFileTileView.fxml")
            );
            Node tile = loader.load();
            ScannedFileTileController controller = loader.getController();
            controller.setFile(file);

            // Clicking a tile opens the preview at that file's position
            tile.setOnMouseClicked(e -> openPreviewAt(currentFiles.indexOf(file)));

            return tile;
        } catch (Exception e) {
            AlertHelper.showError("Display error",
                    "One or more file tiles could not be shown.");
            return new VBox();
        }
    }

    //     Opens the preview overlay showing the file at the given index.

    private void openPreviewAt(int index) {
        if (currentFiles.isEmpty()) return;

        previewIndex = Math.max(0, Math.min(index, currentFiles.size() - 1));
        previewRotation = currentFiles.get(previewIndex).getRotationAngle();

        loadPreviewImage(currentFiles.get(previewIndex));
        topOverview.setVisible(true);
    }


    //  Loads a TIFF

    private void loadPreviewImage(ScannedFile file) {
        String path = file.getFilePath();

        // Placeholder path — no real file yet
        if (path == null || path.isBlank() || path.equals("placeholder")) {
            previewImageView.setImage(null);
            return;
        }

        try {
            java.io.File imageFile = new java.io.File(path);
            if (!imageFile.exists()) {
                previewImageView.setImage(null);
                return;
            }

            BufferedImage buffered = ImageIO.read(imageFile);
            if (buffered == null) {
                AlertHelper.showError("Error", "Could not load image: " + path);
                return;
            }

            Image fxImage = SwingFXUtils.toFXImage(buffered, null);
            previewImageView.setImage(fxImage);

        } catch (IOException e) {
            AlertHelper.showError("Preview error", "Could not load image: " + path);
        }
    }


    @FXML
    private void onBtnPreviousPage(ActionEvent actionEvent) {
        if (currentFiles.isEmpty()) return;
        if (previewIndex > 0) {
            openPreviewAt(previewIndex - 1);
        }
    }

    @FXML
    private void onBtnNext(ActionEvent actionEvent) {
        if (currentFiles.isEmpty()) return;
        if (previewIndex < currentFiles.size() - 1) {
            openPreviewAt(previewIndex + 1);
        }
    }

    @FXML
    private void onBtnRotate(ActionEvent actionEvent) {

    }

    @FXML
    private void onBtnCloseOverview(ActionEvent actionEvent) {
        topOverview.setVisible(false);
    }

    @FXML
    public void onLogout(ActionEvent actionEvent) {
        UserSession.getInstance().clear();
        ViewHandler.EMPLOYEE_DASHBOARD.close();
        ViewHandler.EMPLOYEE_DASHBOARD.reset();
        ViewHandler.LOGIN.show(false);
    }

    @FXML
    private void onBtnStartScanningSession(ActionEvent actionEvent) {
        showChooseProfileModal();
    }

    @FXML private void onBtnExport(ActionEvent actionEvent) {}
}