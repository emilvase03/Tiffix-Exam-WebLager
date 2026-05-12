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

// Java / JavaFX
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
    @FXML private Button     btnRescan;
    @FXML private ScrollPane previewScrollPane;

    private BoxDocumentModel boxDocumentModel;
    private FileImportModel  fileImportModel;
    private ProfileRuleModel profileRuleModel;

    private Document activeDocument = null;
    private final List<ScannedFile> currentFiles = new ArrayList<>();
    private final LinkedHashMap<Document, List<ScannedFile>> sessionData = new LinkedHashMap<>();
    private int nextDocSortOrder = 1;

    private int nextCreationNumber = 1;

    private int previewIndex = 0;
    private final List<Rule> activeRules = new ArrayList<>();

    private final IdentityHashMap<Document, DocumentTileController> documentTileControllers =
            new IdentityHashMap<>();
    private final IdentityHashMap<ScannedFile, ScannedFileTileController> fileTileControllers =
            new IdentityHashMap<>();

     //When the user drags "Document 1" to position 3, its label stays "Document 1"

    private final IdentityHashMap<Document, String> documentLabels = new IdentityHashMap<>();

    private Path scanTempDir;


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
        runFetch(); }

    @FXML
    private void onBtnRescan(ActionEvent event) {
        runFetch(); }

    private void runFetch() {
        setFetchButtonsDisabled(true);

        final int modelSizeBefore = fileImportModel.getScanResults().size();
        final int fileSizeBefore  = currentFiles.size();
        final List<Rule> rules    = List.copyOf(activeRules);

        Task<List<ScannedFile>> task = new Task<>() {
            @Override
            protected List<ScannedFile> call() throws Exception {
                fileImportModel.fetchScansFromApi();

                List<ScannedFile> newFiles = new ArrayList<>();
                var allResults = fileImportModel.getScanResults();
                int scanOrder  = fileSizeBefore + 1;

                for (int i = modelSizeBefore; i < allResults.size(); i++) {
                    var scan = allResults.get(i);
                    Path dest = writeTempFile(scan.fileName(), scan.fileBytes());
                    ScannedFile sf = ScannedFile.unsaved(scanOrder++, dest.toString(), scan.fileBytes());

                    try (ByteArrayInputStream stream = new ByteArrayInputStream(scan.fileBytes())) {
                        BufferedImage raw = ImageIO.read(stream);
                        if (raw != null) {
                            sf.setProcessedImage(ImageTransformations.applyRules(raw, rules));
                        }
                    } catch (IOException ignored) { }

                    newFiles.add(sf);
                }
                return newFiles;
            }
        };

        task.setOnSucceeded(e -> {
            List<ScannedFile> newFiles = task.getValue();
            int firstNewIndex = -1;

            for (ScannedFile f : newFiles) {
                File tiffFile  = new File(f.getFilePath());
                boolean isBarcode = BarcodeDetector.hasBarcode(tiffFile);

                if (isBarcode) {
                    createNewDocument();
                    currentFiles.clear();
                    fileTileControllers.clear();
                    filesTilePane.getChildren().clear();
                } else if (activeDocument == null) {
                    AlertHelper.showError("No document selected",
                            "Scan a barcode page first to start a new document.");
                    break;
                }

                sessionData.get(activeDocument).add(f);
                currentFiles.add(f);
                filesTilePane.getChildren().add(createFileTile(f));
                f.setSortOrder(currentFiles.size());

                if (firstNewIndex == -1) firstNewIndex = currentFiles.size() - 1;
                refreshDocumentTile(activeDocument);
            }

            updateDocumentFileCountLabels();
            if (firstNewIndex != -1) openPreviewAt(firstNewIndex);
            setFetchButtonsDisabled(false);
        });

        task.setOnFailed(e -> {
            Throwable cause = task.getException();
            AlertHelper.showError("Fetch failed",
                    "Could not retrieve files from the scanner API.\n"
                            + (cause != null ? cause.getMessage() : "Unknown error"));
            setFetchButtonsDisabled(false);
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

            activeRules.clear();
            for (var profile : UserSession.getInstance().getActiveProfiles()) {
                try {
                    activeRules.addAll(profileRuleModel.getRulesForProfile(profile));
                } catch (Exception e) {
                    AlertHelper.showError("Rule load error",
                            "Could not load rules for profile: " + profile.getTitle());
                }
            }

            // Full reset
            activeDocument     = null;
            nextDocSortOrder   = documents.size() + 1;
            nextCreationNumber = 1;
            sessionData.clear();
            currentFiles.clear();
            fileTileControllers.clear();
            documentTileControllers.clear();
            documentLabels.clear();
            fileImportModel.clear();
            filesTilePane.getChildren().clear();
            lblTotalFilesInDoc.setText("0");
            topOverview.setVisible(false);

            populateDocumentTilePane(documents);
            lblTotalDocInBox.setText(String.valueOf(documents.size()));
            setTotalsVisible(true);

        } catch (Exception e) {
            AlertHelper.showError("Load error",
                    "Could not load documents for the selected box.");
        }
    }

    private void populateDocumentTilePane(Iterable<Document> documents) {
        documentsTilePane.getChildren().clear();
        documentTileControllers.clear();
        for (Document doc : documents) {
            // Assign a creation label for each DB document in load order
            documentLabels.put(doc, "Document " + nextCreationNumber++);
            documentsTilePane.getChildren().add(createDocumentTile(doc));
        }
    }


    private Node createDocumentTile(Document doc) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/DocumentTileView.fxml"));
            Node tile = loader.load();
            DocumentTileController ctrl = loader.getController();

            ctrl.setDashboardController(this);
            ctrl.setDocument(doc);

            // Apply the immutable creation label
            String label = documentLabels.getOrDefault(doc, "Document ?");
            ctrl.setLabel(label);

            documentTileControllers.put(doc, ctrl);

            List<ScannedFile> existing = sessionData.get(doc);
            if (existing != null && !existing.isEmpty()) {
                ctrl.setFileCount(existing.size());
            }

            tile.setOnMouseClicked(e -> onDocumentSelected(doc));
            return tile;

        } catch (Exception e) {
            AlertHelper.showError("Display error", "A document tile could not be shown.");
            return new VBox();
        }
    }

    //Refreshes the file-count label on a document tile.

    private void refreshDocumentTile(Document doc) {
        DocumentTileController ctrl = documentTileControllers.get(doc);
        if (ctrl == null) return;
        List<ScannedFile> files = sessionData.getOrDefault(doc, Collections.emptyList());
        ctrl.setFileCount(files.size());
        // No setLabel() call here — creation label never changes after first assignment
    }

    private void onDocumentSelected(Document doc) {
        // Highlight selected tile, clear all others
        documentTileControllers.forEach((d, ctrl) -> ctrl.setSelected(false));
        DocumentTileController selected = documentTileControllers.get(doc);
        if (selected != null) selected.setSelected(true);

        activeDocument = doc;
        lblDocumentNr.setText(documentLabels.getOrDefault(doc, String.valueOf(doc.getSortOrder())));

        currentFiles.clear();
        fileTileControllers.clear();
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

        updateDocumentFileCountLabels();
        topOverview.setVisible(false);
    }

    private void createNewDocument() {
        Box box = UserSession.getInstance().getActiveBox();
        Document doc = new Document(-1, box.getId(), nextDocSortOrder++);
        activeDocument = doc;
        sessionData.put(doc, new ArrayList<>());

        // Assign the next creation label before the tile is created
        documentLabels.put(doc, "Document " + nextCreationNumber++);

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
            ctrl.setDashboardController(this);
            fileTileControllers.put(file, ctrl);

            tile.setOnMouseClicked(e -> openPreviewAt(currentFiles.indexOf(file)));
            return tile;

        } catch (Exception e) {
            AlertHelper.showError("Display error", "A file tile could not be shown.");
            return new VBox();
        }
    }

    private void refreshFileTile(ScannedFile file) {
        ScannedFileTileController ctrl = fileTileControllers.get(file);
        if (ctrl != null) ctrl.refresh();
    }

    public void reorderFiles(int draggedScanOrder, ScannedFile target) {
        if (target == null) return;

        ScannedFile dragged = currentFiles.stream()
                .filter(f -> f.getScanOrder() == draggedScanOrder)
                .findFirst().orElse(null);

        if (dragged == null || dragged == target) return;

        int from = currentFiles.indexOf(dragged);
        int to   = currentFiles.indexOf(target);
        if (from == -1 || to == -1) return;

        currentFiles.remove(from);
        currentFiles.add(to, dragged);
        sessionData.put(activeDocument, new ArrayList<>(currentFiles));
        updateFileSortOrders(currentFiles);
        refreshFilePanel();
    }

    private void updateFileSortOrders(List<ScannedFile> files) {
        for (int i = 0; i < files.size(); i++) files.get(i).setSortOrder(i + 1);
    }

    private void refreshFilePanel() {
        filesTilePane.getChildren().clear();
        fileTileControllers.clear();
        for (ScannedFile f : currentFiles) {
            filesTilePane.getChildren().add(createFileTile(f));
        }
        updateDocumentFileCountLabels();
    }

    private void openPreviewAt(int index) {
        if (currentFiles.isEmpty()) return;
        previewIndex = Math.max(0, Math.min(index, currentFiles.size() - 1));
        loadPreviewImage(currentFiles.get(previewIndex));
        topOverview.setVisible(true);
    }

    private void loadPreviewImage(ScannedFile file) {
        BufferedImage base = getOrLoadProcessedImage(file);
        if (base == null) { previewImageView.setImage(null); return; }
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
                file.setProcessedImage(ImageTransformations.applyRules(raw, activeRules));
            }
        } catch (IOException ignored) { }
        return file.getProcessedImage();
    }

    @FXML
    private void onBtnPreviousPage(ActionEvent event) {
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
        loadPreviewImage(current);
        refreshFileTile(current);
    }



    @FXML private void onBtnCloseOverview(ActionEvent event) {
        topOverview.setVisible(false); }


    @FXML
    public void onLogout(ActionEvent event) {
        UserSession.getInstance().clear();
        ViewHandler.EMPLOYEE_DASHBOARD.close();
        ViewHandler.EMPLOYEE_DASHBOARD.reset();
        ViewHandler.LOGIN.show(false);
    }

    @FXML private void onBtnStartScanningSession(ActionEvent event) { showChooseProfileModal(); }

    @FXML
    private void onBtnExport(ActionEvent event) {
        rebuildAllSortOrders();

    }

    public void moveFileToDocument(int draggedScanOrder, Document target) {
        if (target == null || activeDocument == null) return;
        if (target == activeDocument) return;

        ScannedFile file = currentFiles.stream()
                .filter(f -> f.getScanOrder() == draggedScanOrder)
                .findFirst().orElse(null);
        if (file == null) return;

        Document source = activeDocument;

        List<ScannedFile> sourceFiles = sessionData.get(source);
        if (sourceFiles != null) sourceFiles.remove(file);
        currentFiles.remove(file);
        updateFileSortOrders(currentFiles);

        List<ScannedFile> targetFiles = sessionData.computeIfAbsent(target, d -> new ArrayList<>());
        targetFiles.add(file);
        file.setSortOrder(targetFiles.size());

        refreshDocumentTile(source);
        refreshDocumentTile(target);
        refreshFilePanel();
        updateDocumentFileCountLabels();
    }

    public void swapDocuments(int draggedSortOrder, Document target) {
        Document dragged = sessionData.keySet().stream()
                .filter(d -> d.getSortOrder() == draggedSortOrder)
                .findFirst().orElse(null);

        if (dragged == null || target == null || dragged == target) return;

        // Swap visual positions only — labels stay fixed
        int tmp = dragged.getSortOrder();
        dragged.setSortOrder(target.getSortOrder());
        target.setSortOrder(tmp);

        // Rebuild the panel in the new sort order; labels come from documentLabels, not sortOrder
        refreshDocumentPanel();
    }

    private void refreshDocumentPanel() {
        documentsTilePane.getChildren().clear();
        documentTileControllers.clear();

        sessionData.keySet().stream()
                .sorted(Comparator.comparingInt(Document::getSortOrder))
                .forEach(doc -> documentsTilePane.getChildren().add(createDocumentTile(doc)));

        lblTotalDocInBox.setText(String.valueOf(sessionData.size()));
    }

    private void rebuildAllSortOrders() {
        int docOrder = 1;
        for (Map.Entry<Document, List<ScannedFile>> entry : sessionData.entrySet()) {
            entry.getKey().setSortOrder(docOrder++);
            updateFileSortOrders(entry.getValue());
            refreshDocumentTile(entry.getKey());
        }
    }

    private void updateDocumentFileCountLabels() {
        lblTotalFilesInDoc.setText(String.valueOf(currentFiles.size()));
    }

    private void setFetchButtonsDisabled(boolean disabled) {
        if (btnFetch  != null) btnFetch.setDisable(disabled);
        if (btnRescan != null) btnRescan.setDisable(disabled);
    }

    private void setTotalsVisible(boolean visible) {
        lblTotalDocText.setVisible(visible);    lblTotalDocText.setManaged(visible);
        lblTotalDocInBox.setVisible(visible);   lblTotalDocInBox.setManaged(visible);
        lblTotalFilesText.setVisible(visible);  lblTotalFilesText.setManaged(visible);
        lblTotalFilesInDoc.setVisible(visible); lblTotalFilesInDoc.setManaged(visible);
    }

    public void setupPreview() {
        previewScrollPane.viewportBoundsProperty().addListener((obs, old, bounds) -> {
            previewImageView.setFitWidth(bounds.getWidth());
            previewImageView.setFitHeight(bounds.getHeight());
        });
    }

    private Path writeTempFile(String fileName, byte[] bytes) throws IOException {
        if (scanTempDir == null || !Files.exists(scanTempDir)) {
            scanTempDir = Files.createTempDirectory("tiffix-scans-");
        }
        Path dest  = scanTempDir.resolve(fileName);
        int suffix = 1;
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
}