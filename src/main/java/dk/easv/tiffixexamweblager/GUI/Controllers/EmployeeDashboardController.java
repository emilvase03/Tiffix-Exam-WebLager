package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.BLL.Utils.BarcodeDetector;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.DocumentTileController;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.ScannedFileTileController;
import dk.easv.tiffixexamweblager.GUI.Models.BoxDocumentModel;
import dk.easv.tiffixexamweblager.GUI.Models.FileImportModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;

// AtlantaFX
import atlantafx.base.controls.ModalPane;

// Java imports
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.scene.control.ScrollPane;

public class EmployeeDashboardController {
    @FXML private StackPane root;
    @FXML private ModalPane modalPane;
    @FXML private Label lblBoxID;
    @FXML private Label lblDocumentNr;
    @FXML private Label lblTotalDocInBox;
    @FXML private Label lblTotalFilesInDoc;
    @FXML private Label      lblTotalDocText;
    @FXML private Label      lblTotalFilesText;
    @FXML private TilePane   documentsTilePane;
    @FXML private TilePane   filesTilePane;
    @FXML private BorderPane topOverview;
    @FXML private ImageView previewImageView;
    @FXML private Button     btnFetch;


    @FXML
    private ScrollPane previewScrollPane;


    private BoxDocumentModel boxDocumentModel;
    private FileImportModel fileImportModel;
    private Document activeDocument = null;
    private List<ScannedFile> currentFiles = new ArrayList<>();
    private LinkedHashMap<Document, List<ScannedFile>> sessionData = new LinkedHashMap<>();
    private int nextDocSortOrder = 1;
    private int previewIndex = 0;

    //where your app puts scanned files temporarily
    private Path scanTempDir;

    @FXML
    private void initialize() {
        setupPreview();
        try {
            boxDocumentModel = new BoxDocumentModel();
            fileImportModel = new FileImportModel();
            scanTempDir     = Files.createTempDirectory("tiffix-scans-");
        } catch (Exception e) {
            AlertHelper.showError("Documents unavailable",
                    "The documents could not be loaded now.");
        }
        setTotalsVisible(false);
    }


    @FXML
    private void onBtnFetch(ActionEvent event) {
        if (btnFetch != null) btnFetch.setDisable(true);

        // Capture counts on the UI thread BEFORE the background task starts,
        // so we know exactly where the new results begin in the model list.
        final int modelSizeBefore = fileImportModel.getScanResults().size();
        final int fileSizeBefore  = currentFiles.size();

        Task<List<ScannedFile>> task = new Task<>() {
            @Override
            protected List<ScannedFile> call() throws Exception {
                fileImportModel.fetchScansFromApi();

                //  For every newly appended ScanResult: write bytes to a temp
                //    file on disk, then wrap as an unsaved ScannedFile.
                List<ScannedFile> newFiles = new ArrayList<>();
                var allResults = fileImportModel.getScanResults();
                int order = fileSizeBefore + 1;
                for (int i = modelSizeBefore; i < allResults.size(); i++) {
                    var scan = allResults.get(i);
                    Path dest = writeTempFile(scan.fileName(), scan.fileBytes());
                    newFiles.add(ScannedFile.unsaved(order++, dest.toString()));
                }
                return newFiles;
            }
        };

        task.setOnSucceeded(e -> {
            List<ScannedFile> newFiles = task.getValue();

            for (ScannedFile f : newFiles) {
                File tiffFile = new File(f.getFilePath());
                boolean isBarcode = BarcodeDetector.hasBarcode(tiffFile);

                if (isBarcode) {
                    createNewDocument();
                    currentFiles.clear();
                    filesTilePane.getChildren().clear();
                } else if (activeDocument == null) {
                    AlertHelper.showError("No document selected", "Scan a barcode page first to start a new document.");
                    break;
                }

                sessionData.get(activeDocument).add(f);
                currentFiles.add(f);
                filesTilePane.getChildren().add(createFileTile(f));
            }
            lblTotalFilesInDoc.setText(String.valueOf(currentFiles.size()));
            // Auto-open preview on the first newly arrived page
            if (!newFiles.isEmpty()) {
                openPreviewAt(currentFiles.indexOf(newFiles.get(0)));
            }
            if (btnFetch != null) btnFetch.setDisable(false);
        });

        task.setOnFailed(e -> {
            Throwable cause = task.getException();
            AlertHelper.showError("Fetch failed",
                    "Could not retrieve files from the scanner API.\n"
                            + (cause != null ? cause.getMessage() : "Unknown error"));
            if (btnFetch != null) btnFetch.setDisable(false);
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void showChooseProfileModal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/ChooseScanSettingsView.fxml")
            );
            Parent modalContent = loader.load();
            ChooseScanSettingsController controller = loader.getController();
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

            var documents = boxDocumentModel.loadDocumentsForBox(box);
            lblTotalDocInBox.setText(String.valueOf(documents.size()));

            populateDocumentTilePane(documents);

            if (!documents.isEmpty()) {
                activeDocument = documents.get(documents.size() -1);
                nextDocSortOrder = documents.size() + 1;
            } else {
                activeDocument = null;
                nextDocSortOrder = 1;
            }

            // Clear file panel for the new session
            activeDocument = null;
            nextDocSortOrder = boxDocumentModel.loadDocumentsForBox(box).size() + 1;
            sessionData.clear();
            currentFiles.clear();
            fileImportModel.clear();
            filesTilePane.getChildren().clear();
            lblTotalFilesInDoc.setText("0");
            topOverview.setVisible(false);

            setTotalsVisible(true);

        } catch (Exception e) {
            AlertHelper.showError("Load error",
                    "Could not load documents for the selected box.");
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
                    getClass().getResource("/views/DocumentTileView.fxml"));
            Node tile = loader.load();
            DocumentTileController ctrl = loader.getController();
            ctrl.setDocument(doc);
            tile.setOnMouseClicked(e -> onDocumentSelected(doc));
            return tile;
        } catch (Exception e) {
            AlertHelper.showError("Display error",
                    "A document tile could not be shown.");
            return new VBox();
        }
    }

    private void onDocumentSelected(Document doc) {
        activeDocument = doc;
        lblDocumentNr.setText(String.valueOf(doc.getSortOrder()));

        currentFiles.clear();
        filesTilePane.getChildren().clear();

        if (!doc.isUnsaved()) {
            try {
                var dbFiles = boxDocumentModel.loadFilesForDocument(doc);
                sessionData.putIfAbsent(doc, new ArrayList<>(dbFiles));
            } catch (Exception e) {
                AlertHelper.showError("Load error",
                        "Could not load files for the selected document.");
            }
        }

        currentFiles.addAll(sessionData.getOrDefault(doc, new ArrayList<>()));

        for (ScannedFile f : currentFiles) {
            filesTilePane.getChildren().add(createFileTile(f));
        }

        lblTotalFilesInDoc.setText(String.valueOf(currentFiles.size()));
        topOverview.setVisible(false);
    }

    private Node createFileTile(ScannedFile file) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/ScannedFileTileView.fxml"));
            Node tile = loader.load();
            ScannedFileTileController ctrl = loader.getController();
            ctrl.setScannedFile(file);
            tile.setOnMouseClicked(e -> openPreviewAt(currentFiles.indexOf(file)));

            enableDragReorder(tile, file);

            return tile;
        } catch (Exception e) {
            AlertHelper.showError("Display error",
                    "A file tile could not be shown.");
            return new VBox();
        }
    }


    private void createNewDocument() {
        Box box = UserSession.getInstance().getActiveBox();
        Document doc = new Document(-1, box.getId(), nextDocSortOrder++);

        activeDocument = doc;
        sessionData.put(doc, new ArrayList<>());

        documentsTilePane.getChildren().add(createDocumentTile(doc));
        lblTotalDocInBox.setText(String.valueOf(documentsTilePane.getChildren().size()));
    }

//files to show in preview
    private void openPreviewAt(int index) {
        if (currentFiles.isEmpty()) return;
        previewIndex = Math.max(0, Math.min(index, currentFiles.size() - 1));
        loadPreviewImage(currentFiles.get(previewIndex));
        topOverview.setVisible(true);
    }

    private void loadPreviewImage(ScannedFile file) {
        String path = file.getFilePath();

        if (path == null || path.isBlank() || path.equals("placeholder")) {
            previewImageView.setImage(null);
            return;
        }

        File imageFile = new File(path);
        if (!imageFile.exists()) {
            previewImageView.setImage(null);
            return;
        }

        try {
            BufferedImage buffered = ImageIO.read(imageFile);
            if (buffered == null) {
                AlertHelper.showError("Preview error",
                        "Could not decode image: " + path);
                return;
            }
            Image fxImage = SwingFXUtils.toFXImage(buffered, null);
            previewImageView.setImage(fxImage);
            previewImageView.setRotate(file.getRotationAngle());
        } catch (IOException e) {
            AlertHelper.showError("Preview error",
                    "Could not load image: " + path);
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
    private void onBtnNext(ActionEvent event) {
        if (!currentFiles.isEmpty() && previewIndex < currentFiles.size() - 1) {
            openPreviewAt(previewIndex + 1);
        }
    }

    @FXML
    private void onBtnRotate(ActionEvent event) {
        if (currentFiles.isEmpty()) return;
        ScannedFile current = currentFiles.get(previewIndex);
        double newAngle = (current.getRotationAngle() + 90) % 360;
        current.setRotationAngle(newAngle);
        previewImageView.setRotate(newAngle);
    }

    @FXML
    private void onBtnCloseOverview(ActionEvent event) {
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

    @FXML
    private void onBtnExport(ActionEvent event) {
        updateSortOrders();

    }

  //preview, thumbnails, tiles, export
    private Path writeTempFile(String fileName, byte[] bytes) throws IOException {
        if (scanTempDir == null || !Files.exists(scanTempDir)) {
            scanTempDir = Files.createTempDirectory("tiffix-scans-");
        }
//Appends a numeric suffix if a file with the same name already exists.
        Path dest    = scanTempDir.resolve(fileName);
        int  suffix  = 1;
        String base  = fileName;

        while (Files.exists(dest)) {
            int dot = base.lastIndexOf('.');
            String numbered = dot > 0
                    ? base.substring(0, dot) + "_" + suffix + base.substring(dot)
                    : base + "_" + suffix;
            dest = scanTempDir.resolve(numbered);
            suffix++;
        }

        Files.write(dest, bytes);
        return dest;
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

    private void updateSortOrders() {
        for (int i = 0; i < currentFiles.size(); i++) {
            currentFiles.get(i).setSortOrder(i);
        }
    }



    private void enableDragReorder(Node tile, ScannedFile file) {

        tile.setOnDragDetected(e -> {
            Dragboard db = tile.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(file.getFilePath());
            db.setContent(content);
            tile.setOpacity(0.5);
            e.consume();
        });

        tile.setOnDragOver(e -> {
            if (e.getGestureSource() != tile && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        tile.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                Node draggedTile = (Node) e.getGestureSource();

                int draggedIndex = filesTilePane.getChildren().indexOf(draggedTile);
                int targetIndex = filesTilePane.getChildren().indexOf(tile);

                if (draggedIndex != targetIndex) {

                    filesTilePane.getChildren().remove(draggedTile);
                    filesTilePane.getChildren().add(targetIndex, draggedTile);

                    ScannedFile moved = currentFiles.remove(draggedIndex);
                    currentFiles.add(targetIndex, moved);

                    sessionData.put(activeDocument, new ArrayList<>(currentFiles));

                    updateSortOrders();

                }
                success = true;
            }

            e.setDropCompleted(success);
            e.consume();
        });

        tile.setOnDragDone(e -> tile.setOpacity(1));
    }
     public void setupPreview(){

         previewScrollPane.viewportBoundsProperty().addListener((obs, oldVal, bounds) -> {
             previewImageView.setFitWidth(bounds.getWidth());
             previewImageView.setFitHeight(bounds.getHeight());

         });
     }



}