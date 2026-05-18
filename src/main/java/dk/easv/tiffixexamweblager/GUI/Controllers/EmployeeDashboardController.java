package dk.easv.tiffixexamweblager.GUI.Controllers;

//Project imports
import dk.easv.tiffixexamweblager.BE.*;
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

//Antlanta imports
import atlantafx.base.controls.ModalPane;

//JavaFX imports
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javax.imageio.ImageIO;

//Java imports
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
    @FXML private Label      lblTotalFilesInBox;

    @FXML private FlowPane   documentsTilePane;


    @FXML private HBox  boxCell;
    @FXML private Label lblBoxCellTitle;
    @FXML private Label lblBoxCellFileCount;

    @FXML private TilePane   filesTilePane;
    @FXML private BorderPane topOverview;
    @FXML private ImageView  previewImageView;
    @FXML private Button     btnFetch;
    @FXML private Button     btnRescan;
    @FXML private ScrollPane previewScrollPane;
    @FXML private BorderPane dashboardContent;

    private BoxDocumentModel   boxDocumentModel;
    private FileImportModel    fileImportModel;
    private ProfileRuleModel   profileRuleModel;
    private DocumentManager    documentManager;
    private ScannedFileManager scannedFileManager;

    private Document activeDocument = null;
    private final List<ScannedFile>                                       currentFiles            = new ArrayList<>();
    private final LinkedHashMap<Document, List<ScannedFile>>              sessionData             = new LinkedHashMap<>();
    private int nextDocSortOrder   = 1;
    private int nextCreationNumber = 1;
    private int previewIndex       = 0;
    private int selectedFileIndex  = -1;

    private final List<Rule>                                               activeRules             = new ArrayList<>();
    private final IdentityHashMap<Document, DocumentTileController>        documentTileControllers = new IdentityHashMap<>();
    private final IdentityHashMap<ScannedFile, ScannedFileTileController>  fileTileControllers     = new IdentityHashMap<>();
    private final IdentityHashMap<Document, String>                        documentLabels          = new IdentityHashMap<>();

    private Path scanTempDir;


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
            AlertHelper.showError("Documents unavailable", "The documents could not be loaded now.");
        }
        setTotalsVisible(false);
        dashboardContent.prefWidthProperty().bind(root.widthProperty());
        dashboardContent.prefHeightProperty().bind(root.heightProperty());
    }


    @FXML
    private void onBoxCellClicked() {

    }

    // ── Scanning ──────────────────────────────────────────────────────────────

    @FXML private void onBtnFetch(ActionEvent event)  { runFetch(); }
    @FXML private void onBtnRescan(ActionEvent event) { runFetch(); }

    private void runFetch() {
        final int modelSizeBefore = fileImportModel.getScanResults().size();
        final int fileSizeBefore  = currentFiles.size();
        final List<Rule> rules    = List.copyOf(activeRules);

        Task<List<ScannedFile>> task = new Task<>() {
            @Override protected List<ScannedFile> call() throws Exception {
                fileImportModel.fetchScansFromApi();
                List<ScannedFile> newFiles = new ArrayList<>();
                var allResults = fileImportModel.getScanResults();
                int scanOrder = fileSizeBefore + 1;
                for (int i = modelSizeBefore; i < allResults.size(); i++) {
                    var scan = allResults.get(i);
                    byte[] bytes = scan.fileBytes();
                    Path dest = writeTempFile(scan.fileName(), bytes);
                    ScannedFile sf = ScannedFile.unsaved(scanOrder++, dest.toString(), bytes);
                    try (var stream = new ByteArrayInputStream(bytes)) {
                        BufferedImage raw = ImageIO.read(stream);
                        if (raw != null) sf.setProcessedImage(ImageTransformations.applyRules(raw, rules));
                    } catch (IOException ignored) {}
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
                boolean isBarcode = BarcodeDetector.hasBarcode(new File(f.getFilePath()));
                if (isBarcode) {
                    createNewDocument();
                    currentFiles.clear(); fileTileControllers.clear(); filesTilePane.getChildren().clear();
                } else if (activeDocument == null) {
                    AlertHelper.showError("No document selected", "Scan a barcode page first."); return;
                }
                if (selectedFileIndex >= 0 && selectedFileIndex < currentFiles.size()) {
                    sessionData.get(activeDocument).set(selectedFileIndex, f);
                    currentFiles.set(selectedFileIndex, f);
                    filesTilePane.getChildren().set(selectedFileIndex, createFileTile(f));
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
            updateTotalFilesInBoxLabel();
            if (firstNewIndex != -1) openPreviewAt(firstNewIndex);
        });

        task.setOnFailed(e -> AlertHelper.showError("Fetch failed",
                task.getException() != null ? task.getException().getMessage() : "Unknown error"));

        new Thread(task).start();
    }

    // ── Export ────────────────────────────────────────────────────────────────

    @FXML
    private void onBtnExport(ActionEvent event) {
        if (sessionData.isEmpty() || sessionData.values().stream().allMatch(List::isEmpty)) {
            AlertHelper.showError("Nothing to export", "There are no scanned files to export."); return;
        }

        ButtonType btnSingle = new ButtonType("Single-page TIFFs");
        ButtonType btnMulti  = new ButtonType("Multi-page TIFF");
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Export"); alert.setHeaderText("Choose export format");
        alert.setContentText("Single-page: one .tiff per page.\nMulti-page: one .tiff per document.");
        alert.getButtonTypes().setAll(btnSingle, btnMulti, btnCancel);

        TextArea notesField = new TextArea();
        notesField.setPromptText("Enter notes for this box (optional)");
        notesField.setWrapText(true);
        notesField.setPrefRowCount(3);
        notesField.setStyle("-fx-padding: 0px");
        alert.getDialogPane().setExpandableContent(notesField);
        alert.getDialogPane().setExpanded(true);

        Optional<ButtonType> choice = alert.showAndWait();
        if (choice.isEmpty() || choice.get() == btnCancel) return;
        boolean multiPage = choice.get() == btnMulti;
        final String notes = notesField.getText().trim().isEmpty() ? null : notesField.getText().trim();

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Export Folder");
        File dir = chooser.showDialog(root.getScene().getWindow());
        if (dir == null) return;

        Path outputDir = dir.toPath();
        rebuildAllSortOrders();
        Map<Document, List<ScannedFile>> snapshot = new LinkedHashMap<>(sessionData);
        final List<Rule> rules = List.copyOf(activeRules);

        Task<String> exportTask = new Task<>() {
            @Override protected String call() throws Exception {
                TiffExportService svc = new TiffExportService();
                StringBuilder sb = new StringBuilder();
                Document doc = null;

                int boxId = snapshot.keySet().iterator().next().getBoxId();

                for (var entry : snapshot.entrySet()) {
                    doc = entry.getKey();
                    List<ScannedFile> files = entry.getValue();
                    if (files.isEmpty()) continue;
                    String label = documentLabels.getOrDefault(doc, "Document_" + doc.getSortOrder());

                    if (doc.isUnsaved()) {
                        Document c = documentManager.createDocument(doc.getBoxId(), doc.getSortOrder());
                        doc.setId(c.getId());
                    }
                    scannedFileManager.saveFilesForDocument(doc.getId(), files);

                    if (multiPage) {
                        int w = svc.exportMultiPage(files, outputDir.resolve(sanitizeLabel(label) + ".tiff"), rules);
                        sb.append(label).append(": ").append(w).append(" page(s)\n");
                    } else {
                        svc.exportSinglePage(files, outputDir, label, rules);
                        sb.append(label).append(": ").append(files.size()).append(" file(s)\n");
                    }
                }

                int documentsAmount = (int) snapshot.values().stream().filter(f -> !f.isEmpty()).count();
                int totalFilesAmount = snapshot.values().stream().mapToInt(List::size).sum();
                boxDocumentModel.createMetadata(new Metadata(boxId, documentsAmount, totalFilesAmount, notes));

                return sb.toString().trim();
            }
        };
        exportTask.setOnSucceeded(e -> { Alert d = new Alert(Alert.AlertType.INFORMATION); d.setTitle("Export complete"); d.setHeaderText("Saved to: " + outputDir); d.setContentText(exportTask.getValue()); d.showAndWait(); });
        exportTask.setOnFailed(e -> AlertHelper.showError("Export failed", exportTask.getException() != null ? exportTask.getException().getMessage() : "Unknown error"));
        new Thread(exportTask).start();
    }

    private String sanitizeLabel(String s) { return s.replaceAll("[\\s/\\\\:*?\"<>|]", "_"); }

    // ── Session ───────────────────────────────────────────────────────────────

    @FXML private void onBtnStartScanningSession(ActionEvent event) { showChooseProfileModal(); }

    private void showChooseProfileModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ChooseScanSettingsView.fxml"));
            Parent content = loader.load();
            ChooseScanSettingsController ctrl = loader.getController();
            ctrl.init(modalPane, this::onSessionStarted);
            modalPane.show(content);
        } catch (Exception e) {
            AlertHelper.showError("Unable to open profile selection", "Could not open profile selection.");
        }
    }

    private void onSessionStarted() {
        try {
            Box box = UserSession.getInstance().getActiveBox();
            lblBoxID.setText(String.valueOf(box.getNumber()));
            var documents = boxDocumentModel.loadDocumentsForBox(box);

            activeRules.clear();
            for (var profile : UserSession.getInstance().getActiveProfiles()) {
                try { activeRules.addAll(profileRuleModel.getRulesForProfile(profile)); }
                catch (Exception e) { AlertHelper.showError("Rule load error", "Could not load rules for: " + profile.getTitle()); }
            }

            activeDocument = null;
            nextDocSortOrder = documents.size() + 1;
            nextCreationNumber = 1;
            sessionData.clear(); currentFiles.clear();
            fileTileControllers.clear(); documentTileControllers.clear(); documentLabels.clear();
            fileImportModel.clear(); filesTilePane.getChildren().clear();
            lblTotalFilesInDoc.setText("0"); topOverview.setVisible(false);
            lblBoxCellTitle.setText("Box " + box.getNumber());
            boxCell.setVisible(true); boxCell.setManaged(true);

            populateDocumentTiles(documents);
            lblTotalDocInBox.setText(String.valueOf(documents.size()));

            for (Document doc : documents) {
                try { sessionData.put(doc, new ArrayList<>(boxDocumentModel.loadFilesForDocument(doc))); refreshDocumentTile(doc); }
                catch (Exception e) { sessionData.put(doc, new ArrayList<>()); }
            }

            setTotalsVisible(true);
            updateTotalFilesInBoxLabel();

        } catch (Exception e) {
            AlertHelper.showError("Load error", "Could not load documents for the selected box.");
        }
    }

    private void refreshDocumentPanel() {
        documentTileControllers.clear();
        documentsTilePane.getChildren().clear();
        sessionData.keySet().stream()
                .sorted(Comparator.comparingInt(Document::getSortOrder))
                .forEach(doc -> documentsTilePane.getChildren().add(createDocumentTile(doc)));
        lblTotalDocInBox.setText(String.valueOf(sessionData.size()));
    }

    private void populateDocumentTiles(Iterable<Document> documents) {
        documentTileControllers.clear();
        documentsTilePane.getChildren().clear();
        for (Document doc : documents) {
            documentLabels.put(doc, "Document " + nextCreationNumber++);
            documentsTilePane.getChildren().add(createDocumentTile(doc));
        }
    }

    private Node createDocumentTile(Document doc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DocumentTileView.fxml"));
            Node tile = loader.load();
            DocumentTileController ctrl = loader.getController();
            ctrl.setDashboardController(this);
            ctrl.setDocument(doc);
            ctrl.setLabel(documentLabels.getOrDefault(doc, "Document ?"));
            ctrl.setContainerWidthProperty(documentsTilePane.widthProperty());
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
        ctrl.setFileCount(sessionData.getOrDefault(doc, Collections.emptyList()).size());
    }

    private void onDocumentSelected(Document doc) {
        documentTileControllers.forEach((d, c) -> c.setSelected(false));
        DocumentTileController sel = documentTileControllers.get(doc);
        if (sel != null) sel.setSelected(true);

        activeDocument = doc;
        lblDocumentNr.setText(documentLabels.getOrDefault(doc, String.valueOf(doc.getSortOrder())));

        currentFiles.clear(); fileTileControllers.clear(); filesTilePane.getChildren().clear();

        if (!doc.isUnsaved()) {
            try { sessionData.putIfAbsent(doc, new ArrayList<>(boxDocumentModel.loadFilesForDocument(doc))); }
            catch (Exception e) { AlertHelper.showError("Load error", "Could not load files."); }
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
        // TASK 1: add to FlowPane
        documentsTilePane.getChildren().add(createDocumentTile(doc));
        lblTotalDocInBox.setText(String.valueOf(sessionData.size()));
    }

    // ── File panel ────────────────────────────────────────────────────────────

    private Node createFileTile(ScannedFile file) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ScannedFileTileView.fxml"));
            Node tile = loader.load();
            ScannedFileTileController ctrl = loader.getController();
            ctrl.setScannedFile(file); ctrl.setDashboardController(this);
            fileTileControllers.put(file, ctrl);
            tile.setOnMouseClicked(e -> { selectedFileIndex = currentFiles.indexOf(file); openPreviewAt(selectedFileIndex); });
            return tile;
        } catch (Exception e) {
            AlertHelper.showError("Display error", "A file tile could not be shown."); return new VBox();
        }
    }

    private void refreshFilePanel() {
        filesTilePane.getChildren().clear(); fileTileControllers.clear();
        for (ScannedFile f : currentFiles) filesTilePane.getChildren().add(createFileTile(f));
        updateDocumentFileCountLabels();
    }

    private void refreshFileTile(ScannedFile file) {
        ScannedFileTileController ctrl = fileTileControllers.get(file);
        if (ctrl != null) ctrl.refresh();
    }

    // ── Drag-reorder ──────────────────────────────────────────────────────────

    public void reorderFiles(int draggedScanOrder, ScannedFile target) {
        if (target == null) return;
        ScannedFile dragged = currentFiles.stream().filter(f -> f.getScanOrder() == draggedScanOrder).findFirst().orElse(null);
        if (dragged == null || dragged == target) return;
        int from = currentFiles.indexOf(dragged), to = currentFiles.indexOf(target);
        if (from == -1 || to == -1) return;
        currentFiles.remove(from); currentFiles.add(to, dragged);
        sessionData.put(activeDocument, new ArrayList<>(currentFiles));
        updateFileSortOrders(currentFiles); refreshFilePanel();
    }

    public void moveFileToDocument(int draggedScanOrder, Document target) {
        if (target == null || activeDocument == null || target == activeDocument) return;
        ScannedFile file = currentFiles.stream().filter(f -> f.getScanOrder() == draggedScanOrder).findFirst().orElse(null);
        if (file == null) return;
        Document source = activeDocument;
        List<ScannedFile> src = sessionData.get(source); if (src != null) src.remove(file);
        currentFiles.remove(file); updateFileSortOrders(currentFiles);
        List<ScannedFile> tgt = sessionData.computeIfAbsent(target, d -> new ArrayList<>());
        tgt.add(file); file.setSortOrder(tgt.size());
        refreshDocumentTile(source); refreshDocumentTile(target);
        refreshFilePanel(); updateDocumentFileCountLabels(); updateTotalFilesInBoxLabel();
    }

    public void swapDocuments(int draggedSortOrder, Document target) {
        Document dragged = sessionData.keySet().stream().filter(d -> d.getSortOrder() == draggedSortOrder).findFirst().orElse(null);
        if (dragged == null || target == null || dragged == target) return;
        int tmp = dragged.getSortOrder(); dragged.setSortOrder(target.getSortOrder()); target.setSortOrder(tmp);
        refreshDocumentPanel();
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
        previewImageView.setImage(SwingFXUtils.toFXImage(
                ImageTransformations.applyAll(base, file.getUserRotation(), file.getUserBrightness()), null));
        previewImageView.setRotate(0);
    }

    private BufferedImage getOrLoadProcessedImage(ScannedFile file) {
        if (file.getProcessedImage() != null) return file.getProcessedImage();
        byte[] bytes = file.getTiffFile();
        if (bytes != null && bytes.length > 0) {
            try { BufferedImage raw = ImageIO.read(new ByteArrayInputStream(bytes)); if (raw != null) { file.setProcessedImage(ImageTransformations.applyRules(raw, activeRules)); return file.getProcessedImage(); } } catch (IOException ignored) {}
        }
        String path = file.getFilePath();
        if (path != null && !path.isBlank()) {
            try { BufferedImage raw = ImageIO.read(new File(path)); if (raw != null) file.setProcessedImage(ImageTransformations.applyRules(raw, activeRules)); } catch (IOException ignored) {}
        }
        return file.getProcessedImage();
    }

    @FXML private void onBtnPreviousPage(ActionEvent e) { if (!currentFiles.isEmpty() && previewIndex > 0) openPreviewAt(previewIndex - 1); }
    @FXML private void onBtnNext(ActionEvent e)         { if (!currentFiles.isEmpty() && previewIndex < currentFiles.size() - 1) openPreviewAt(previewIndex + 1); }

    @FXML private void onBtnRotate(ActionEvent e) {
        if (currentFiles.isEmpty()) return;
        ScannedFile cur = currentFiles.get(previewIndex);
        cur.setUserRotation((cur.getUserRotation() + 90) % 360);
        loadPreviewImage(cur); refreshFileTile(cur);
    }

    @FXML private void onBtnCloseOverview(ActionEvent e) { topOverview.setVisible(false); }

    @FXML public void onLogout(ActionEvent e) {
        UserSession.getInstance().clear();
        ViewHandler.EMPLOYEE_DASHBOARD.close(); ViewHandler.EMPLOYEE_DASHBOARD.reset(); ViewHandler.LOGIN.show(false);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void rebuildAllSortOrders() {
        int o = 1;
        for (var entry : sessionData.entrySet()) {
            entry.getKey().setSortOrder(o++); updateFileSortOrders(entry.getValue()); refreshDocumentTile(entry.getKey());
        }
    }

    private void updateFileSortOrders(List<ScannedFile> files) {
        for (int i = 0; i < files.size(); i++) files.get(i).setSortOrder(i + 1);
    }

    private void updateDocumentFileCountLabels() { lblTotalFilesInDoc.setText(String.valueOf(currentFiles.size())); }

    private void updateTotalFilesInBoxLabel() {
        if (lblTotalFilesInBox != null)
            lblTotalFilesInBox.setText(String.valueOf(sessionData.values().stream().mapToInt(List::size).sum()));
    }

    private void setTotalsVisible(boolean v) {
        lblTotalDocText.setVisible(v);    lblTotalDocText.setManaged(v);
        lblTotalDocInBox.setVisible(v);   lblTotalDocInBox.setManaged(v);
        lblTotalFilesText.setVisible(v);  lblTotalFilesText.setManaged(v);
        lblTotalFilesInDoc.setVisible(v); lblTotalFilesInDoc.setManaged(v);
    }

    public void setupPreview() {
        previewScrollPane.viewportBoundsProperty().addListener((obs, old, b) -> {
            previewImageView.setFitWidth(b.getWidth()); previewImageView.setFitHeight(b.getHeight());
        });
    }

    private Path writeTempFile(String fileName, byte[] bytes) throws IOException {
        if (scanTempDir == null || !Files.exists(scanTempDir)) scanTempDir = Files.createTempDirectory("tiffix-scans-");
        Path dest = scanTempDir.resolve(fileName); int suffix = 1; String base = fileName;
        while (Files.exists(dest)) {
            int dot = base.lastIndexOf('.');
            dest = scanTempDir.resolve(dot > 0 ? base.substring(0, dot) + "_" + suffix + base.substring(dot) : base + "_" + suffix);
            suffix++;
        }
        Files.write(dest, bytes); return dest;
    }
}