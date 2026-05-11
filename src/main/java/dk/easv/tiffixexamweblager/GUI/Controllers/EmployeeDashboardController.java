package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.BLL.Utils.BarcodeDetector;
import dk.easv.tiffixexamweblager.BLL.Utils.ImageTransformations;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.DocumentTileController;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.ScannedFileTileController;
import dk.easv.tiffixexamweblager.GUI.Models.BoxDocumentModel;
import dk.easv.tiffixexamweblager.GUI.Models.FileImportModel;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileRuleModel;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class EmployeeDashboardController {

    @FXML private StackPane  root;
    @FXML private ModalPane  modalPane;
    @FXML private Label      lblBoxID;
    @FXML private Label      lblDocumentNr;
    @FXML private Label      lblTotalDocInBox;
    @FXML private Label      lblTotalFilesInDoc;
    @FXML private Label      lblTotalDocText;
    @FXML private Label      lblTotalFilesText;
    @FXML private TilePane   documentsTilePane;
    @FXML private TilePane   filesTilePane;
    @FXML private BorderPane topOverview;
    @FXML private ImageView  previewImageView;
    @FXML private Button     btnFetch;
    @FXML private ScrollPane previewScrollPane;



    private BoxDocumentModel boxDocumentModel;
    private FileImportModel  fileImportModel;
    private ProfileRuleModel profileRuleModel;


    private Document                         activeDocument   = null;
    private final List<ScannedFile>          currentFiles     = new ArrayList<>();
    private final LinkedHashMap<Document, List<ScannedFile>> sessionData = new LinkedHashMap<>();
    private int nextDocSortOrder = 1;
    private int previewIndex     = 0;


    private List<Rule> activeRules = new ArrayList<>();



    private final IdentityHashMap<ScannedFile, ScannedFileTileController> tileControllers =
            new IdentityHashMap<>();

    private Path scanTempDir;
    public static final DataFormat FILE_FORMAT =
            new DataFormat("application/x-scanned-file");

    public static final DataFormat DOC_FORMAT =
            new DataFormat("application/x-document");



    @FXML
    private void initialize() {
        setupPreview();
        try {
            boxDocumentModel = new BoxDocumentModel();
            fileImportModel  = new FileImportModel();
            profileRuleModel = new ProfileRuleModel();
            scanTempDir      = Files.createTempDirectory("tiffix-scans-");
        } catch (Exception e) {
            AlertHelper.showError("Documents unavailable",
                    "The documents could not be loaded now.");
        }
        setTotalsVisible(false);
    }


    @FXML
    private void onBtnFetch(ActionEvent event) {
        if (btnFetch != null) btnFetch.setDisable(true);

        final int modelSizeBefore = fileImportModel.getScanResults().size();
        final int fileSizeBefore  = currentFiles.size();
        // Snapshot rules for safe use on the background thread
        final List<Rule> rules = List.copyOf(activeRules);

        Task<List<ScannedFile>> task = new Task<>() {
            @Override
            protected List<ScannedFile> call() throws Exception {
                fileImportModel.fetchScansFromApi();

                List<ScannedFile> newFiles = new ArrayList<>();
                var allResults = fileImportModel.getScanResults();
                int order = fileSizeBefore + 1;

                for (int i = modelSizeBefore; i < allResults.size(); i++) {
                    var scan = allResults.get(i);

                    // Write bytes to a temp file so BarcodeDetector can read the File
                    Path dest = writeTempFile(scan.fileName(), scan.fileBytes());

                    // Build ScannedFile with raw bytes for later export
                    ScannedFile sf = ScannedFile.unsaved(order++, dest.toString(), scan.fileBytes());

                    // Decode image and apply profile rules immediately on the background thread
                    // so the UI thread never blocks on pixel processing
                    try (ByteArrayInputStream stream = new ByteArrayInputStream(scan.fileBytes())) {
                        BufferedImage raw = ImageIO.read(stream);
                        if (raw != null) {
                            sf.setProcessedImage(ImageTransformations.applyRules(raw, rules));
                        }
                    } catch (IOException ignored) {
                        // Image unreadable — tile will show blank placeholder
                    }

                    newFiles.add(sf);
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
                    tileControllers.clear();
                    filesTilePane.getChildren().clear();
                } else if (activeDocument == null) {
                    AlertHelper.showError("No document selected",
                            "Scan a barcode page first to start a new document.");
                    break;
                }

                sessionData.get(activeDocument).add(f);
                currentFiles.add(f);
                filesTilePane.getChildren().add(createFileTile(f));
            }

            lblTotalFilesInDoc.setText(String.valueOf(currentFiles.size()));
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
                    getClass().getResource("/views/ChooseScanSettingsView.fxml"));
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
            populateDocumentTilePane(documents);
            lblTotalDocInBox.setText(String.valueOf(documents.size()));

            //  Load rules for every active profile
            // These are stored here and applied to each image at fetch time.
            activeRules.clear();
            for (var profile : UserSession.getInstance().getActiveProfiles()) {
                try {
                    activeRules.addAll(profileRuleModel.getRulesForProfile(profile));
                } catch (Exception e) {
                    AlertHelper.showError("Rule load error",
                            "Could not load rules for profile: " + profile.getTitle());
                }
            }

            activeDocument   = null;
            nextDocSortOrder = documents.size() + 1;
            sessionData.clear();
            currentFiles.clear();
            tileControllers.clear();
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
            ctrl.setDashboardController(this);
            tile.setOnMouseClicked(e -> onDocumentSelected(doc));
            return tile;
        } catch (Exception e) {
            AlertHelper.showError("Display error", "A document tile could not be shown.");
            return new VBox();
        }
    }

    private void onDocumentSelected(Document doc) {
        activeDocument = doc;
        lblDocumentNr.setText(String.valueOf(doc.getSortOrder()));

        currentFiles.clear();
        tileControllers.clear();
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

    private void createNewDocument() {
        Box box = UserSession.getInstance().getActiveBox();
        Document doc = new Document(-1, box.getId(), nextDocSortOrder++);
        activeDocument = doc;
        sessionData.put(doc, new ArrayList<>());
        documentsTilePane.getChildren().add(createDocumentTile(doc));
        lblTotalDocInBox.setText(String.valueOf(documentsTilePane.getChildren().size()));
    }


    private Node createFileTile(ScannedFile file) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/ScannedFileTileView.fxml"));
            Node tile = loader.load();
            ScannedFileTileController ctrl = loader.getController();
            ctrl.setScannedFile(file);
            tileControllers.put(file, ctrl);   // store so we can refresh later

            tile.setOnMouseClicked(e -> openPreviewAt(currentFiles.indexOf(file)));
            enableDragReorder(tile, file);
            return tile;
        } catch (Exception e) {
            AlertHelper.showError("Display error", "A file tile could not be shown.");
            return new VBox();
        }
    }


    private void openPreviewAt(int index) {
        if (currentFiles.isEmpty()) return;
        previewIndex = Math.max(0, Math.min(index, currentFiles.size() - 1));
        loadPreviewImage(currentFiles.get(previewIndex));
        topOverview.setVisible(true);
    }


    private void loadPreviewImage(ScannedFile file) {
        BufferedImage base = getOrLoadProcessedImage(file);
        if (base == null) {
            previewImageView.setImage(null);
            return;
        }

        BufferedImage display = ImageTransformations.applyAll(
                base, file.getUserRotation(), file.getUserBrightness());
        previewImageView.setImage(SwingFXUtils.toFXImage(display, null));
        previewImageView.setRotate(0);
    }


    private BufferedImage getOrLoadProcessedImage(ScannedFile file) {
        if (file.getProcessedImage() != null) return file.getProcessedImage();

        String path = file.getFilePath();
        if (path == null || path.isBlank()) return null;

        try {
            BufferedImage raw = ImageIO.read(new File(path));
            if (raw != null) {
                // Apply active rules and cache — same treatment as freshly fetched files
                BufferedImage processed = ImageTransformations.applyRules(raw, activeRules);
                file.setProcessedImage(processed);
            }
        } catch (IOException ignored) {

        }
        return file.getProcessedImage();
    }


    @FXML
    private void onBtnPreviousPage(ActionEvent actionEvent) {
        if (!currentFiles.isEmpty() && previewIndex > 0) openPreviewAt(previewIndex - 1);
    }

    @FXML
    private void onBtnNext(ActionEvent event) {
        if (!currentFiles.isEmpty() && previewIndex < currentFiles.size() - 1)
            openPreviewAt(previewIndex + 1);
    }

    @FXML
    private void onBtnRotate(ActionEvent event) {
        if (currentFiles.isEmpty()) return;
        ScannedFile current = currentFiles.get(previewIndex);
        current.setUserRotation((current.getUserRotation() + 90) % 360);
        loadPreviewImage(current);      // refresh large preview
        refreshTile(current);           // refresh thumbnail in the tile strip
    }


    private void refreshTile(ScannedFile file) {
        ScannedFileTileController ctrl = tileControllers.get(file);
        if (ctrl != null) ctrl.refresh();
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
            boolean success = false;
            if (e.getDragboard().hasString()) {
                Node draggedTile  = (Node) e.getGestureSource();
                int draggedIndex  = filesTilePane.getChildren().indexOf(draggedTile);
                int targetIndex   = filesTilePane.getChildren().indexOf(tile);

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


    private Path writeTempFile(String fileName, byte[] bytes) throws IOException {
        if (scanTempDir == null || !Files.exists(scanTempDir)) {
            scanTempDir = Files.createTempDirectory("tiffix-scans-");
        }
        Path dest   = scanTempDir.resolve(fileName);
        int  suffix = 1;
        String base = fileName;
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


    private void updateSortOrders() {
        for (int i = 0; i < currentFiles.size(); i++) {
            currentFiles.get(i).setSortOrder(i + 1);
        }
    }
    private void updateDocumentSortOrders() {
        int order = 1;
        for (Document doc : sessionData.keySet()) {
            doc.setSortOrder(order++);
            refreshDocumentTile(doc);
        }
        lblTotalDocInBox.setText(String.valueOf(sessionData.size()));
    }
    private void refreshDocumentTile(Document document) {

        for (Node node : documentsTilePane.getChildren()) {

            // Each tile is a VBox controlled by DocumentTileController
            Object controller = node.getUserData();

            if (controller instanceof DocumentTileController tileController) {

                if (tileController.getDocument().equals(document)) {

                    // Re-bind the same document to force UI refresh
                    tileController.setDocument(document);
                    return;
                }
            }
        }
    }
    private void setTotalsVisible(boolean visible) {
        lblTotalDocText.setVisible(visible);    lblTotalDocText.setManaged(visible);
        lblTotalDocInBox.setVisible(visible);   lblTotalDocInBox.setManaged(visible);
        lblTotalFilesText.setVisible(visible);  lblTotalFilesText.setManaged(visible);
        lblTotalFilesInDoc.setVisible(visible); lblTotalFilesInDoc.setManaged(visible);
    }

    public void setupPreview() {
        previewScrollPane.viewportBoundsProperty().addListener((obs, oldVal, bounds) -> {
            previewImageView.setFitWidth(bounds.getWidth());
            previewImageView.setFitHeight(bounds.getHeight());
        });
    }
    public void removeFileFromCurrentDoc(ScannedFile file) {

        if (file == null || activeDocument == null) return;

        List<ScannedFile> files = sessionData.get(activeDocument);
        if (files != null) {
            files.remove(file);
        }

        currentFiles.remove(file);

        refreshDocumentTile(activeDocument);
        refreshFilePanel();
    }

    public void addFileToDocument(ScannedFile file, Document targetDoc) {

        if (file == null || targetDoc == null) return;


        // Remove from current document first
        removeFileFromCurrentDoc(file);

        sessionData
                .computeIfAbsent(targetDoc, d -> new ArrayList<>())
                .add(file);

        refreshDocumentTile(targetDoc);
    }
    public void swapDocuments(Document dragged, Document target) {
        if (dragged == null || target == null || dragged == target) return;

        int tmp = dragged.getSortOrder();
        dragged.setSortOrder(target.getSortOrder());
        target.setSortOrder(tmp);

        refreshDocumentPanel();
    }
    private void refreshFilePanel() {
        filesTilePane.getChildren().clear();

        for (ScannedFile f : currentFiles) {
            filesTilePane.getChildren().add(createFileTile(f));
        }

        lblTotalFilesInDoc.setText(String.valueOf(currentFiles.size()));
    }
    private void refreshDocumentPanel() {
        documentsTilePane.getChildren().clear();

        sessionData.keySet().stream()
                .sorted(Comparator.comparingInt(Document::getSortOrder))
                .forEach(doc ->
                        documentsTilePane.getChildren().add(createDocumentTile(doc))
                );
    }
    public void reorderFiles(ScannedFile dragged, ScannedFile target) {
        if (dragged == null || target == null || dragged == target) return;

        int from = currentFiles.indexOf(dragged);
        int to   = currentFiles.indexOf(target);

        if (from == -1 || to == -1) return;

        currentFiles.remove(from);
        currentFiles.add(to, dragged);

        refreshFilePanel();
    }

}