package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.BLL.DocumentManager;
import dk.easv.tiffixexamweblager.BLL.ScannedFileManager;
import dk.easv.tiffixexamweblager.BLL.Utils.BarcodeDetector;
import dk.easv.tiffixexamweblager.BLL.Utils.ImageTransformations;
import dk.easv.tiffixexamweblager.BLL.Utils.TiffExportService;
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
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

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

    private BoxDocumentModel   boxDocumentModel;
    private FileImportModel    fileImportModel;
    private ProfileRuleModel   profileRuleModel;
    private DocumentManager    documentManager;
    private ScannedFileManager scannedFileManager;

    private Document activeDocument = null;
    private final List<ScannedFile> currentFiles = new ArrayList<>();
    private final LinkedHashMap<Document, List<ScannedFile>> sessionData = new LinkedHashMap<>();
    private int nextDocSortOrder  = 1;
    private int nextCreationNumber = 1;

    private int previewIndex = 0;
    private final List<Rule> activeRules = new ArrayList<>();

    private final IdentityHashMap<Document, DocumentTileController>       documentTileControllers = new IdentityHashMap<>();
    private final IdentityHashMap<ScannedFile, ScannedFileTileController> fileTileControllers     = new IdentityHashMap<>();
    private final IdentityHashMap<Document, String>                       documentLabels          = new IdentityHashMap<>();

    private Path scanTempDir;
    @FXML
    private Label lblTotalFilesInBox;
    private int selectedFileIndex = -1;


    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @FXML
    private void initialize() {
        setupPreview();
        try {
            boxDocumentModel   = new BoxDocumentModel();
            fileImportModel    = new FileImportModel();
            profileRuleModel   = new ProfileRuleModel();
            documentManager    = new DocumentManager();
            scannedFileManager = new ScannedFileManager();
            scanTempDir        = Files.createTempDirectory("tiffix-scans-");
        } catch (Exception e) {
            AlertHelper.showError("Documents unavailable",
                    "The documents could not be loaded now.");
        }
        setTotalsVisible(false);
    }

    // ── Scanning ──────────────────────────────────────────────────────────────

    @FXML private void onBtnFetch(ActionEvent event)  {
        runFetch(); }

    @FXML private void onBtnRescan(ActionEvent event) {
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
                    var scan  = allResults.get(i);
                    byte[] bytes = scan.fileBytes();

                    Path dest = writeTempFile(scan.fileName(), bytes);
                    ScannedFile sf = ScannedFile.unsaved(scanOrder++, dest.toString(), bytes);

                    try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
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

            if (!newFiles.isEmpty()) {
                ScannedFile f = newFiles.get(0);
                File tiffFile     = new File(f.getFilePath());
                boolean isBarcode = BarcodeDetector.hasBarcode(tiffFile);

                if (isBarcode) {
                    createNewDocument();
                    currentFiles.clear();
                    fileTileControllers.clear();
                    filesTilePane.getChildren().clear();
                } else if (activeDocument == null) {
                    AlertHelper.showError("No document selected",
                            "Scan a barcode page first to start a new document.");
                    return;
                }

                if (selectedFileIndex >= 0 && selectedFileIndex < currentFiles.size()) {

                    sessionData.get(activeDocument).set(selectedFileIndex, f);
                    currentFiles.set(selectedFileIndex, f);

                    Node newTile = createFileTile(f);
                    filesTilePane.getChildren().set(selectedFileIndex, newTile);

                    f.setSortOrder(selectedFileIndex + 1);

                    openPreviewAt(selectedFileIndex);

                } else {

                    sessionData.get(activeDocument).add(f);
                    currentFiles.add(f);
                    filesTilePane.getChildren().add(createFileTile(f));

                    f.setSortOrder(currentFiles.size());

                    if (firstNewIndex == -1) firstNewIndex = currentFiles.size() - 1;
                }

                refreshDocumentTile(activeDocument);
            }

            updateDocumentFileCountLabels();
            updateTotalFilesInBoxLabel();   // keep box-total in sync after each fetch
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

    // ── Export ────────────────────────────────────────────────────────────────

    @FXML
    private void onBtnExport(ActionEvent event) {
        if (sessionData.isEmpty() || sessionData.values().stream().allMatch(List::isEmpty)) {
            AlertHelper.showError("Nothing to export", "There are no scanned files to export.");
            return;
        }

        // 1. Ask: single-page or multi-page
        ButtonType btnSingle = new ButtonType("Single-page TIFFs");
        ButtonType btnMulti  = new ButtonType("Multi-page TIFF");
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert typeAlert = new Alert(Alert.AlertType.CONFIRMATION);
        typeAlert.setTitle("Export");
        typeAlert.setHeaderText("Choose export format");
        typeAlert.setContentText(
                "Single-page: one .tiff file per page.\n" +
                        "Multi-page:  one .tiff file per document containing all its pages.");
        typeAlert.getButtonTypes().setAll(btnSingle, btnMulti, btnCancel);

        Optional<ButtonType> choice = typeAlert.showAndWait();
        if (choice.isEmpty() || choice.get() == btnCancel) return;

        boolean multiPage = (choice.get() == btnMulti);

        // 2. Pick output directory
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Export Folder");
        File dir = chooser.showDialog(root.getScene().getWindow());
        if (dir == null) return;

        Path outputDir = dir.toPath();

        // 3. Rebuild sort orders so everything is consistent before saving
        rebuildAllSortOrders();

        // Snapshot — shallow copy of the map so the background thread has a stable key set
        Map<Document, List<ScannedFile>> snapshot = new LinkedHashMap<>(sessionData);

        // 4. Save to DB + export on a background thread
        Task<String> exportTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                TiffExportService exportService = new TiffExportService();
                StringBuilder summary = new StringBuilder();

                for (Map.Entry<Document, List<ScannedFile>> entry : snapshot.entrySet()) {
                    Document doc   = entry.getKey();
                    List<ScannedFile> files = entry.getValue();
                    if (files.isEmpty()) continue;

                    String docLabel = documentLabels.getOrDefault(doc,
                            "Document_" + doc.getSortOrder());

                    // ── Save document to DB if unsaved ────────────────────────
                    if (doc.isUnsaved()) {
                        Document created = documentManager.createDocument(
                                doc.getBoxId(), doc.getSortOrder());
                        doc.setId(created.getId());
                    }

                    // ── Save all unsaved files to DB ──────────────────────────
                    scannedFileManager.saveFilesForDocument(doc.getId(), files);

                    // ── Export to disk ────────────────────────────────────────
                    if (multiPage) {
                        String filename = sanitizeLabel(docLabel) + ".tiff";
                        Path   outFile  = outputDir.resolve(filename);
                        int    written  = exportService.exportMultiPage(files, outFile);
                        summary.append(docLabel)
                                .append(": ").append(written).append(" page(s) written\n");
                    } else {
                        exportService.exportSinglePage(files, outputDir, docLabel);
                        summary.append(docLabel)
                                .append(": ").append(files.size()).append(" file(s) written\n");
                    }
                }

                return summary.toString().trim();
            }
        };

        exportTask.setOnSucceeded(e -> {
            Alert done = new Alert(Alert.AlertType.INFORMATION);
            done.setTitle("Export complete");
            done.setHeaderText("Files saved to: " + outputDir);
            done.setContentText(exportTask.getValue());
            done.showAndWait();
        });

        exportTask.setOnFailed(e -> {
            Throwable cause = exportTask.getException();
            AlertHelper.showError("Export failed",
                    cause != null ? cause.getMessage() : "Unknown error");
        });

        Thread t = new Thread(exportTask);
        t.setDaemon(true);
        t.start();
    }

    private String sanitizeLabel(String label) {
        return label.replaceAll("[\\s/\\\\:*?\"<>|]", "_");
    }

    // ── Session ───────────────────────────────────────────────────────────────

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

            // Reset BEFORE reading counts — the old session's data must be gone first
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

            for (Document doc : documents) {
                try {
                    List<ScannedFile> dbFiles = boxDocumentModel.loadFilesForDocument(doc);
                    sessionData.put(doc, new ArrayList<>(dbFiles));
                    refreshDocumentTile(doc);
                } catch (Exception e) {
                    sessionData.put(doc, new ArrayList<>());
                }
            }

            setTotalsVisible(true);
            updateTotalFilesInBoxLabel();

        } catch (Exception e) {
            AlertHelper.showError("Load error",
                    "Could not load documents for the selected box.");
        }
    }
    private int getTotalFilesInBox() {
        return sessionData.values()
                .stream()
                .mapToInt(List::size)
                .sum();
    }

    private void updateTotalFilesInBoxLabel() {
        if (lblTotalFilesInBox != null) {
            lblTotalFilesInBox.setText(String.valueOf(getTotalFilesInBox()));
        }
    }

    // ── Document tile panel ───────────────────────────────────────────────────

    private void populateDocumentTilePane(Iterable<Document> documents) {
        documentsTilePane.getChildren().clear();
        documentTileControllers.clear();
        for (Document doc : documents) {
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
            ctrl.setLabel(documentLabels.getOrDefault(doc, "Document ?"));
            documentTileControllers.put(doc, ctrl);

            List<ScannedFile> existing = sessionData.get(doc);
            if (existing != null && !existing.isEmpty()) ctrl.setFileCount(existing.size());

            tile.setOnMouseClicked(e -> onDocumentSelected(doc));
            return tile;

        } catch (Exception e) {
            AlertHelper.showError("Display error", "A document tile could not be shown.");
            return new VBox();
        }
    }

    private void refreshDocumentTile(Document doc) {
        DocumentTileController ctrl = documentTileControllers.get(doc);
        if (ctrl == null) return;
        List<ScannedFile> files = sessionData.getOrDefault(doc, Collections.emptyList());
        ctrl.setFileCount(files.size());
    }

    private void onDocumentSelected(Document doc) {
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
        for (ScannedFile f : currentFiles) filesTilePane.getChildren().add(createFileTile(f));

        updateDocumentFileCountLabels();
        topOverview.setVisible(false);
    }

    private void createNewDocument() {
        Box box = UserSession.getInstance().getActiveBox();
        Document doc = new Document(-1, box.getId(), nextDocSortOrder++);
        activeDocument = doc;
        sessionData.put(doc, new ArrayList<>());
        documentLabels.put(doc, "Document " + nextCreationNumber++);
        documentsTilePane.getChildren().add(createDocumentTile(doc));
        lblTotalDocInBox.setText(String.valueOf(documentsTilePane.getChildren().size()));
    }

    // ── File tile panel ───────────────────────────────────────────────────────

    private Node createFileTile(ScannedFile file) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/ScannedFileTileView.fxml"));
            Node tile = loader.load();
            ScannedFileTileController ctrl = loader.getController();

            ctrl.setScannedFile(file);
            ctrl.setDashboardController(this);
            fileTileControllers.put(file, ctrl);

            tile.setOnMouseClicked(e -> {
                int index = currentFiles.indexOf(file);
                selectedFileIndex = index;
                openPreviewAt(index);
            });

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

    // ── Preview ───────────────────────────────────────────────────────────────

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

        byte[] bytes = file.getTiffFile();
        if (bytes != null && bytes.length > 0) {
            try {
                BufferedImage raw = ImageIO.read(new ByteArrayInputStream(bytes));
                if (raw != null) {
                    file.setProcessedImage(ImageTransformations.applyRules(raw, activeRules));
                    return file.getProcessedImage();
                }
            } catch (IOException ignored) { }
        }

        String path = file.getFilePath();
        if (path != null && !path.isBlank()) {
            try {
                BufferedImage raw = ImageIO.read(new File(path));
                if (raw != null) {
                    file.setProcessedImage(ImageTransformations.applyRules(raw, activeRules));
                }
            } catch (IOException ignored) { }
        }

        return file.getProcessedImage();
    }

    // ── Navigation ────────────────────────────────────────────────────────────

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

    @FXML private void onBtnCloseOverview(ActionEvent event) { topOverview.setVisible(false); }

    @FXML
    public void onLogout(ActionEvent event) {
        UserSession.getInstance().clear();
        ViewHandler.EMPLOYEE_DASHBOARD.close();
        ViewHandler.EMPLOYEE_DASHBOARD.reset();
        ViewHandler.LOGIN.show(false);
    }

    @FXML private void onBtnStartScanningSession(ActionEvent event) { showChooseProfileModal(); }

    // ── Drag-reorder ──────────────────────────────────────────────────────────

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
        updateTotalFilesInBoxLabel();   // count across all documents is unchanged but stay consistent
    }

    public void swapDocuments(int draggedSortOrder, Document target) {
        Document dragged = sessionData.keySet().stream()
                .filter(d -> d.getSortOrder() == draggedSortOrder)
                .findFirst().orElse(null);

        if (dragged == null || target == null || dragged == target) return;

        int tmp = dragged.getSortOrder();
        dragged.setSortOrder(target.getSortOrder());
        target.setSortOrder(tmp);

        refreshDocumentPanel();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void refreshFilePanel() {
        filesTilePane.getChildren().clear();
        fileTileControllers.clear();
        for (ScannedFile f : currentFiles) filesTilePane.getChildren().add(createFileTile(f));
        updateDocumentFileCountLabels();
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

    private void updateFileSortOrders(List<ScannedFile> files) {
        for (int i = 0; i < files.size(); i++) files.get(i).setSortOrder(i + 1);
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