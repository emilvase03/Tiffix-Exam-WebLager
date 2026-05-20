package dk.easv.tiffixexamweblager.GUI.Controllers;

//Project imports
import dk.easv.tiffixexamweblager.BE.*;
import dk.easv.tiffixexamweblager.BLL.DocumentManager;
import dk.easv.tiffixexamweblager.BLL.ScannedFileManager;
import dk.easv.tiffixexamweblager.BLL.Utils.BarcodeDetector;
import dk.easv.tiffixexamweblager.BLL.Utils.ImageTransformations;
import dk.easv.tiffixexamweblager.BLL.Utils.TiffExportService;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.ScannedFileTileController;
import dk.easv.tiffixexamweblager.GUI.Models.BoxDocumentModel;
import dk.easv.tiffixexamweblager.GUI.Models.FileImportModel;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileRuleModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import dk.easv.tiffixexamweblager.GUI.Utils.ShortcutRegistry;
import dk.easv.tiffixexamweblager.GUI.Utils.ViewHandler;

//Atlanta imports
import atlantafx.base.controls.ModalPane;
import javafx.scene.Scene;
import javafx.scene.input.*;

// Ikonli imports
import org.kordamp.ikonli.javafx.FontIcon;

//JavaFX imports
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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

    @FXML private StackPane        root;
    @FXML private ModalPane        modalPane;
    @FXML private Label            lblBoxID;
    @FXML private Label            lblDocumentNr;
    @FXML private Label            lblTotalDocInBox;
    @FXML private Label            lblTotalFilesInDoc;
    @FXML private Label            lblTotalDocText;
    @FXML private Label            lblTotalFilesText;
    @FXML private Label            lblTotalFilesInBox;
    @FXML private TreeView<Object> treeView;
    @FXML private TilePane         filesTilePane;
    @FXML private BorderPane       topOverview;
    @FXML private ImageView        previewImageView;
    @FXML private Button           btnFetch;
    @FXML private Button           btnRescan;
    @FXML private ScrollPane       previewScrollPane;
    @FXML private BorderPane       dashboardContent;

    private BoxDocumentModel   boxDocumentModel;
    private FileImportModel    fileImportModel;
    private ProfileRuleModel   profileRuleModel;
    private DocumentManager    documentManager;
    private ScannedFileManager scannedFileManager;

    private Document      activeDocument = null;
    private ScannedFile   activeFile     = null;

    private final List<ScannedFile>                                       currentFiles        = new ArrayList<>();
    private final LinkedHashMap<Document, List<ScannedFile>>              sessionData         = new LinkedHashMap<>();
    private int nextDocSortOrder   = 1;
    private int nextCreationNumber = 1;
    private int previewIndex       = 0;
    private int selectedFileIndex  = -1;

    private final List<Rule>                                              activeRules         = new ArrayList<>();
    private final IdentityHashMap<ScannedFile, ScannedFileTileController> fileTileControllers = new IdentityHashMap<>();
    private final IdentityHashMap<Document, String>                       documentLabels      = new IdentityHashMap<>();

    private TreeItem<Object>                                      boxTreeItem       = null;
    private final IdentityHashMap<Document,    TreeItem<Object>>  documentTreeItems = new IdentityHashMap<>();
    private final IdentityHashMap<ScannedFile, TreeItem<Object>>  fileTreeItems     = new IdentityHashMap<>();

    private static final double TREE_THUMB_W = 32.0;
    private static final double TREE_THUMB_H = 42.0;

    private Path        scanTempDir;
    private ScannedFile draggedFile;
    private Document    draggedDocument;

    private static final String TREE_DRAG_OVER = "tree-doc-drag-over";

    private final ShortcutRegistry shortcutRegistry = new ShortcutRegistry();


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

        dashboardContent.prefWidthProperty().bind(root.widthProperty());
        dashboardContent.prefHeightProperty().bind(root.heightProperty());

        if (treeView == null) {
            AlertHelper.showError("Layout error",
                    "TreeView missing from FXML. Add <TreeView fx:id=\"treeView\"> to EmployeeDashboardView.fxml.");
            return;
        }

        treeView.setCellFactory(tv -> createTreeCell());
        treeView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.SINGLE);
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            Object val = sel.getValue();
            if      (val instanceof Box)            onBoxSelected();
            else if (val instanceof Document  d)    onDocumentSelected(d);
            else if (val instanceof ScannedFile f)  onTreeFileSelected(f);
        });

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null)
                shortcutRegistry.detach();
            if (newScene != null)
                registerShortcuts(newScene);
        });
    }

    private void registerShortcuts(Scene scene) {
        shortcutRegistry
                .register(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN), this::onLogout)
                .register(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), this::onBtnStartScanningSession)
                .register(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), this::onBtnFetch)
                .register(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::onBtnExport)
                .register(new KeyCodeCombination(KeyCode.ESCAPE, KeyCombination.SHIFT_ANY), this::onBtnCloseOverview)
                .register(new KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.SHIFT_ANY), this::onBtnRescan)
                .register(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN), this::onBtnRotate)
                .register(new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHIFT_ANY), this::onBtnNext)
                .register(new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHIFT_ANY), this::onBtnPreviousPage)
                .attach(scene);

        treeView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                TreeItem<Object> selected = treeView.getSelectionModel().getSelectedItem();
                if (selected != null && !selected.isLeaf()) {
                    selected.setExpanded(!selected.isExpanded());
                }
                event.consume();
                return;
            }

            if (event.getCode() != KeyCode.TAB)
                return;

            int current = treeView.getSelectionModel().getSelectedIndex();
            int next = event.isShiftDown() ? current - 1 : current + 1;

            if (next >= 0 && next < treeView.getExpandedItemCount()) {
                treeView.getSelectionModel().select(next);
                treeView.scrollTo(next);
            }

            event.consume();
        });
    }

    private void assignAllBoxFileNames() {
        int counter = 1;
        List<Map.Entry<Document, List<ScannedFile>>> sorted = new ArrayList<>(sessionData.entrySet());
        sorted.sort(Comparator.comparingInt(e -> e.getKey().getSortOrder()));
        for (Map.Entry<Document, List<ScannedFile>> entry : sorted)
            for (ScannedFile f : entry.getValue())
                f.assignOrderName(counter++);
    }

    private void assignAllBoxDocumentNames() {
        List<Document> sorted = new ArrayList<>(sessionData.keySet());
        sorted.sort(Comparator.comparingInt(Document::getSortOrder));
        int counter = 1;
        for (Document doc : sorted) {
            doc.assignOrderName(counter++);
            documentLabels.put(doc, doc.getOrderName());
        }
    }

    private void refreshTileLabels() {
        fileTileControllers.forEach((file, ctrl) -> ctrl.refresh());
    }


    public void setDraggedFile(ScannedFile file)     { this.draggedFile     = file; }
    public ScannedFile getDraggedFile()              { return draggedFile; }

    public void setDraggedDocument(Document doc)     { this.draggedDocument = doc; }
    public Document getDraggedDocument()             { return draggedDocument; }


    private TreeCell<Object> createTreeCell() {
        return new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("tree-box-cell", "tree-doc-cell", "tree-file-cell");
                // Reset drag handlers so recycled cells don't keep stale ones
                setOnDragDetected(null); setOnDragDone(null);
                setOnDragOver(null); setOnDragEntered(null);
                setOnDragExited(null); setOnDragDropped(null);

                if (empty || item == null) { setText(null); setGraphic(null); return; }

                if (item instanceof Box b) {
                    getStyleClass().add("tree-box-cell");
                    int total = sessionData.values().stream().mapToInt(List::size).sum();
                    setText("Box " + b.getNumber() + "  ("
                            + total + (total == 1 ? " file" : " files") + ")");
                    setGraphic(makeIcon("/img/Box.png", 28));

                } else if (item instanceof Document d) {
                    getStyleClass().add("tree-doc-cell");
                    int    count = sessionData.getOrDefault(d, Collections.emptyList()).size();
                    String lbl   = documentLabels.getOrDefault(d, "Document " + d.getSortOrder());
                    setText(count > 0 ? lbl + "  (" + count + ")" : lbl);
                    setGraphic(makeIcon("/img/FileI.png", 22));

                    setOnDragDetected(e -> {
                        Dragboard db = startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent cc = new ClipboardContent();
                        cc.putString("DOC");
                        db.setContent(cc);
                        setDraggedDocument(d);
                        setOpacity(0.5);
                        e.consume();
                    });
                    setOnDragDone(e -> {
                        setOpacity(1.0);
                        setDraggedDocument(null);
                    });

                    setOnDragOver(e -> {
                        if (e.getDragboard().hasString()
                                && e.getDragboard().getString().equals("DOC")
                                && getDraggedDocument() != d)       // skip self
                            e.acceptTransferModes(TransferMode.MOVE);
                        e.consume();
                    });
                    setOnDragEntered(e -> {
                        if (e.getDragboard().hasString()
                                && e.getDragboard().getString().equals("DOC")
                                && getDraggedDocument() != d)
                            getStyleClass().add(TREE_DRAG_OVER);   // visual highlight
                        e.consume();
                    });
                    setOnDragExited(e -> {
                        getStyleClass().remove(TREE_DRAG_OVER);
                        e.consume();
                    });
                    setOnDragDropped(e -> {
                        getStyleClass().remove(TREE_DRAG_OVER);
                        boolean ok = false;
                        if (e.getDragboard().hasString()
                                && e.getDragboard().getString().equals("DOC")) {
                            Document src = getDraggedDocument();
                            if (src != null && src != d) {
                                moveDocument(src, d);
                                ok = true;
                            }
                        }
                        e.setDropCompleted(ok);
                        e.consume();
                    });

                } else if (item instanceof ScannedFile f) {
                    getStyleClass().add("tree-file-cell");
                    String name = f.getFileName();
                    setText(name != null && !name.isBlank() ? name : "File " + f.getScanOrder());
                    setGraphic(fileThumbnail(f));
                }
            }
        };
    }

    private ImageView makeIcon(String cp, double size) {
        var stream = getClass().getResourceAsStream(cp);
        if (stream == null) return null;
        var iv = new ImageView(new Image(stream));
        iv.setFitWidth(size); iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        iv.getStyleClass().add("tree-icon");
        return iv;
    }

    private Node fileThumbnail(ScannedFile f) {
        BufferedImage bi = getOrLoadProcessedImage(f);
        if (bi == null) {
            FontIcon fi = new FontIcon("bi-file-earmark");
            fi.getStyleClass().add("tree-icon");
            return fi;
        }
        var fxImg = SwingFXUtils.toFXImage(
                ImageTransformations.applyAll(bi, f.getUserRotation(), f.getUserBrightness()), null);
        var iv = new ImageView(fxImg);
        iv.setFitWidth(TREE_THUMB_W); iv.setFitHeight(TREE_THUMB_H);
        iv.setPreserveRatio(true); iv.setSmooth(true);
        iv.getStyleClass().add("tree-thumb");
        return iv;
    }

    // Tree build

    private void buildTree(Box box) {
        documentTreeItems.clear(); fileTreeItems.clear();
        boxTreeItem = new TreeItem<>(box);
        boxTreeItem.setExpanded(true);
        treeView.setRoot(boxTreeItem);
        treeView.setShowRoot(true);
    }

    private void populateTreeFromDocuments(List<Document> documents) {
        if (boxTreeItem == null) return;
        boxTreeItem.getChildren().clear();
        documentTreeItems.clear(); fileTreeItems.clear();
        for (Document doc : documents) {
            TreeItem<Object> docItem = new TreeItem<>(doc);
            docItem.setExpanded(false);
            boxTreeItem.getChildren().add(docItem);
            documentTreeItems.put(doc, docItem);
        }
    }

    private void addFileToTree(Document doc, ScannedFile file) {
        TreeItem<Object> docItem = documentTreeItems.get(doc);
        if (docItem == null || fileTreeItems.containsKey(file)) { treeView.refresh(); return; }
        TreeItem<Object> fi = new TreeItem<>(file);
        docItem.getChildren().add(fi);
        fileTreeItems.put(file, fi);
        treeView.refresh();
    }

    private void removeFileFromTree(ScannedFile file) {
        TreeItem<Object> fi = fileTreeItems.remove(file);
        if (fi != null && fi.getParent() != null) fi.getParent().getChildren().remove(fi);
        treeView.refresh();
    }

    private void refreshTreeFileNodes(Document doc) {
        TreeItem<Object> docItem = documentTreeItems.get(doc); if (docItem == null) return;
        for (TreeItem<Object> child : new ArrayList<>(docItem.getChildren()))
            fileTreeItems.remove(child.getValue());
        docItem.getChildren().clear();
        for (ScannedFile f : sessionData.getOrDefault(doc, Collections.emptyList())) {
            TreeItem<Object> fi = new TreeItem<>(f);
            docItem.getChildren().add(fi); fileTreeItems.put(f, fi);
        }
        treeView.refresh();
    }

    private void addDocumentToTree(Document doc) {
        if (boxTreeItem == null) return;
        TreeItem<Object> docItem = new TreeItem<>(doc);
        docItem.setExpanded(true);
        boxTreeItem.getChildren().add(docItem);
        documentTreeItems.put(doc, docItem);
        treeView.refresh();
    }


    private void onBoxSelected() {
        activeDocument = null; activeFile = null;
        currentFiles.clear(); fileTileControllers.clear(); filesTilePane.getChildren().clear();
        sessionData.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getSortOrder()))
                .forEach(entry -> entry.getValue().forEach(f -> {
                    currentFiles.add(f);
                    filesTilePane.getChildren().add(createFileTile(f));
                }));
        lblDocumentNr.setText("All documents");
        lblTotalFilesInDoc.setText(String.valueOf(currentFiles.size()));
        topOverview.setVisible(false);
    }

    private void onDocumentSelected(Document doc) {
        activeDocument = doc; activeFile = null;
        lblDocumentNr.setText(documentLabels.getOrDefault(doc, String.valueOf(doc.getSortOrder())));

        if (!doc.isUnsaved()) {
            try {
                if (!sessionData.containsKey(doc)) {
                    List<ScannedFile> loaded = new ArrayList<>(boxDocumentModel.loadFilesForDocument(doc));
                    sessionData.put(doc, loaded);
                    assignAllBoxFileNames();
                    refreshTreeFileNodes(doc);
                }
            } catch (Exception e) {
                AlertHelper.showError("Load error", "Could not load files for the selected document.");
            }
        }

        currentFiles.clear(); fileTileControllers.clear(); filesTilePane.getChildren().clear();
        currentFiles.addAll(sessionData.getOrDefault(doc, new ArrayList<>()));
        for (ScannedFile f : currentFiles) filesTilePane.getChildren().add(createFileTile(f));
        updateDocumentFileCountLabels();
        topOverview.setVisible(false);
    }

    private void onTreeFileSelected(ScannedFile file) {
        Document parentDoc = null;
        for (Map.Entry<Document, List<ScannedFile>> entry : sessionData.entrySet())
            if (entry.getValue().contains(file)) { parentDoc = entry.getKey(); break; }
        if (parentDoc == null) return;

        activeDocument = parentDoc; activeFile = file;
        currentFiles.clear(); fileTileControllers.clear(); filesTilePane.getChildren().clear();
        currentFiles.addAll(sessionData.getOrDefault(parentDoc, new ArrayList<>()));
        for (ScannedFile f : currentFiles) filesTilePane.getChildren().add(createFileTile(f));

        int idx = currentFiles.indexOf(file); selectedFileIndex = Math.max(idx, 0);
        lblDocumentNr.setText(documentLabels.getOrDefault(parentDoc, "Document " + parentDoc.getSortOrder()));
        updateDocumentFileCountLabels();
        openPreviewAt(selectedFileIndex);
    }

    // ── Scanning ──────────────────────────────────────────────────────────────

    @FXML private void onBtnFetch()  {
        runFetch(); }

    @FXML private void onBtnRescan() {
        runFetch(); }

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
                    var scan = allResults.get(i); byte[] bytes = scan.fileBytes();
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
                if (BarcodeDetector.hasBarcode(new File(f.getFilePath()))) {
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
                    refreshTreeFileNodes(activeDocument);
                } else {
                    sessionData.get(activeDocument).add(f);
                    currentFiles.add(f);
                    filesTilePane.getChildren().add(createFileTile(f));
                    f.setSortOrder(currentFiles.size());
                    if (firstNewIndex == -1) firstNewIndex = currentFiles.size() - 1;
                    addFileToTree(activeDocument, f);
                }
                assignAllBoxFileNames();
                refreshTileLabels();
            }
            updateDocumentFileCountLabels();
            updateTotalFilesInBoxLabel();
            treeView.refresh();
            if (firstNewIndex != -1) openPreviewAt(firstNewIndex);
        });

        task.setOnFailed(e -> AlertHelper.showError("Fetch failed",
                "Could not retrieve files.\n"
                        + (task.getException() != null ? task.getException().getMessage() : "")));
        new Thread(task).start();
    }

    // ── Export ────────────────────────────────────────────────────────────────

    @FXML private void onBtnExport() {
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
        notesField.setWrapText(true); notesField.setPrefRowCount(3); notesField.setStyle("-fx-padding: 0px");
        alert.getDialogPane().setExpandableContent(notesField); alert.getDialogPane().setExpanded(true);
        Optional<ButtonType> choice = alert.showAndWait();
        if (choice.isEmpty() || choice.get() == btnCancel) return;
        boolean multiPage = choice.get() == btnMulti;
        final String notes = notesField.getText().trim().isEmpty() ? null : notesField.getText().trim();
        DirectoryChooser chooser = new DirectoryChooser(); chooser.setTitle("Select Export Folder");
        File dir = chooser.showDialog(root.getScene().getWindow()); if (dir == null) return;
        Path outputDir = dir.toPath();
        rebuildAllSortOrders();
        Map<Document, List<ScannedFile>> snapshot = new LinkedHashMap<>(sessionData);
        final List<Rule> rules = List.copyOf(activeRules);

        Task<String> exportTask = new Task<>() {
            @Override protected String call() throws Exception {
                TiffExportService svc = new TiffExportService();
                StringBuilder sb = new StringBuilder();
                int boxId = snapshot.keySet().iterator().next().getBoxId();
                for (var entry : snapshot.entrySet()) {
                    Document doc = entry.getKey(); List<ScannedFile> files = entry.getValue();
                    if (files.isEmpty()) continue;
                    String lbl = documentLabels.getOrDefault(doc, "Document_" + doc.getSortOrder());
                    if (doc.isUnsaved()) {
                        Document c = documentManager.createDocument(doc.getBoxId(), doc.getSortOrder());
                        doc.setId(c.getId());
                    }
                    scannedFileManager.saveFilesForDocument(doc.getId(), files);
                    if (multiPage) {
                        int w = svc.exportMultiPage(files, outputDir.resolve(sanitizeLabel(lbl) + ".tiff"), rules);
                        sb.append(lbl).append(": ").append(w).append(" page(s)\n");
                    } else {
                        svc.exportSinglePage(files, outputDir, lbl, rules);
                        sb.append(lbl).append(": ").append(files.size()).append(" file(s)\n");
                    }
                }
                int docAmt  = (int) snapshot.values().stream().filter(f -> !f.isEmpty()).count();
                int fileAmt = snapshot.values().stream().mapToInt(List::size).sum();
                boxDocumentModel.saveMetadata(new Metadata(boxId, docAmt, fileAmt, notes));
                return sb.toString().trim();
            }
        };
        exportTask.setOnSucceeded(e -> {
            Alert d = new Alert(Alert.AlertType.INFORMATION);
            d.setTitle("Export complete"); d.setHeaderText("Saved to: " + outputDir);
            d.setContentText(exportTask.getValue()); d.showAndWait();
        });
        exportTask.setOnFailed(e -> AlertHelper.showError("Export failed",
                exportTask.getException() != null ? exportTask.getException().getMessage() : "Unknown error"));
        new Thread(exportTask).start();
    }

    private String sanitizeLabel(String label) {
        return label.replaceAll("[\\s/\\\\:*?\"<>|]", "_"); }

    // ── Session ───────────────────────────────────────────────────────────────

    @FXML private void onBtnStartScanningSession() {
        showChooseProfileModal(); }

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
                catch (Exception e) {
                    AlertHelper.showError("Rule load error", "Could not load rules for: " + profile.getTitle());
                }
            }

            activeDocument = null; activeFile = null;
            nextDocSortOrder = documents.size() + 1; nextCreationNumber = 1;
            sessionData.clear(); currentFiles.clear(); fileTileControllers.clear();
            documentLabels.clear(); fileImportModel.clear();
            filesTilePane.getChildren().clear();
            lblTotalFilesInDoc.setText("0"); topOverview.setVisible(false);

            buildTree(box);
            populateTreeFromDocuments(documents);
            lblTotalDocInBox.setText(String.valueOf(documents.size()));

            for (Document doc : documents) {
                try {
                    List<ScannedFile> files = new ArrayList<>(boxDocumentModel.loadFilesForDocument(doc));
                    sessionData.put(doc, files);
                    TreeItem<Object> docItem = documentTreeItems.get(doc);
                    if (docItem != null) {
                        for (ScannedFile f : files) {
                            TreeItem<Object> fi = new TreeItem<>(f);
                            docItem.getChildren().add(fi); fileTreeItems.put(f, fi);
                        }
                    }
                } catch (Exception e) { sessionData.put(doc, new ArrayList<>()); }
            }

            assignAllBoxDocumentNames();
            assignAllBoxFileNames();

            setTotalsVisible(true);
            updateTotalFilesInBoxLabel();
            treeView.refresh();
            treeView.getSelectionModel().select(boxTreeItem);

        } catch (Exception e) {
            AlertHelper.showError("Load error", "Could not load documents for the selected box.");
        }
    }

    // ── Document management ───────────────────────────────────────────────────

    private void refreshDocumentPanel() {
        if (boxTreeItem == null) return;
        boxTreeItem.getChildren().sort((a, b) -> {
            if (a.getValue() instanceof Document da && b.getValue() instanceof Document db)
                return Integer.compare(da.getSortOrder(), db.getSortOrder());
            return 0;
        });
        treeView.refresh();
        lblTotalDocInBox.setText(String.valueOf(sessionData.size()));
    }

    private void createNewDocument() {
        Box box = UserSession.getInstance().getActiveBox();
        Document doc = new Document(-1, box.getId(), nextDocSortOrder++);
        activeDocument = doc;
        sessionData.put(doc, new ArrayList<>());
        addDocumentToTree(doc);
        assignAllBoxDocumentNames();
        lblTotalDocInBox.setText(String.valueOf(sessionData.size()));
    }

    // ── File panel ────────────────────────────────────────────────────────────

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
                int idx = currentFiles.indexOf(file);
                selectedFileIndex = idx;
                openPreviewAt(idx);
            });
            return tile;
        } catch (Exception e) {
            AlertHelper.showError("Display error", "A file tile could not be shown: " + e.getMessage());
            return new VBox();
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
        treeView.refresh();
    }

    public void reorderFiles(ScannedFile dragged, ScannedFile target) {
        if (dragged == null || target == null || dragged == target) return;
        int from = currentFiles.indexOf(dragged), to = currentFiles.indexOf(target);
        if (from == -1 || to == -1) return;
        currentFiles.remove(from);
        currentFiles.add(to, dragged);
        sessionData.put(activeDocument, new ArrayList<>(currentFiles));
        updateFileSortOrders(currentFiles);
        assignAllBoxFileNames();
        onDocumentSelected(activeDocument);
        treeView.refresh();
        draggedFile = null;
    }

    public void moveFileToDocument(ScannedFile file, Document target) {
        if (file == null || target == null || activeDocument == null || target == activeDocument) return;
        Document source = activeDocument;
        List<ScannedFile> sourceFiles = sessionData.get(source);
        if (sourceFiles != null) sourceFiles.remove(file);
        currentFiles.remove(file);
        updateFileSortOrders(currentFiles);
        List<ScannedFile> targetFiles = sessionData.computeIfAbsent(target, d -> new ArrayList<>());
        targetFiles.add(file); file.setSortOrder(targetFiles.size());
        removeFileFromTree(file); addFileToTree(target, file);
        assignAllBoxFileNames();
        onDocumentSelected(source);
        updateTotalFilesInBoxLabel();
        treeView.refresh();
        draggedFile = null;
    }


    public void moveDocument(Document dragged, Document target) {
        if (dragged == null || target == null || dragged == target) return;

        List<Document> ordered = new ArrayList<>(sessionData.keySet());
        ordered.sort(Comparator.comparingInt(Document::getSortOrder));

        int fromIdx = ordered.indexOf(dragged);
        int toIdx   = ordered.indexOf(target);
        if (fromIdx == -1 || toIdx == -1) return;


        ordered.remove(fromIdx);

        toIdx = ordered.indexOf(target);
        ordered.add(toIdx, dragged);

        for (int i = 0; i < ordered.size(); i++)
            ordered.get(i).setSortOrder(i + 1);

        refreshDocumentPanel();

        assignAllBoxDocumentNames();
        assignAllBoxFileNames();

        if (activeDocument != null)
            lblDocumentNr.setText(documentLabels.getOrDefault(activeDocument,
                    String.valueOf(activeDocument.getSortOrder())));

        refreshTileLabels();
        draggedDocument = null;
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

    BufferedImage getOrLoadProcessedImage(ScannedFile file) {
        if (file.getProcessedImage() != null) return file.getProcessedImage();
        byte[] bytes = file.getTiffFile();
        if (bytes != null && bytes.length > 0) {
            try {
                BufferedImage raw = ImageIO.read(new ByteArrayInputStream(bytes));
                if (raw != null) {
                    file.setProcessedImage(ImageTransformations.applyRules(raw, activeRules));
                    return file.getProcessedImage();
                }
            } catch (IOException ignored) {}
        }
        String path = file.getFilePath();
        if (path != null && !path.isBlank()) {
            try {
                BufferedImage raw = ImageIO.read(new File(path));
                if (raw != null) file.setProcessedImage(ImageTransformations.applyRules(raw, activeRules));
            } catch (IOException ignored) {}
        }
        return file.getProcessedImage();
    }

    @FXML private void onBtnPreviousPage() {
        if (!currentFiles.isEmpty() && previewIndex > 0) openPreviewAt(previewIndex - 1);
    }
    @FXML private void onBtnNext() {
        if (!currentFiles.isEmpty() && previewIndex < currentFiles.size() - 1) openPreviewAt(previewIndex + 1);
    }
    @FXML private void onBtnRotate() {
        if (currentFiles.isEmpty()) return;
        ScannedFile cur = currentFiles.get(previewIndex);
        cur.setUserRotation((cur.getUserRotation() + 90) % 360);
        loadPreviewImage(cur); refreshFileTile(cur);
    }
    @FXML private void onBtnCloseOverview() { topOverview.setVisible(false); }

    @FXML public void onLogout() {
        UserSession.getInstance().clear();
        ViewHandler.EMPLOYEE_DASHBOARD.close();
        ViewHandler.EMPLOYEE_DASHBOARD.reset();
        ViewHandler.LOGIN.show(false);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void rebuildAllSortOrders() {
        int order = 1;
        for (Map.Entry<Document, List<ScannedFile>> e : sessionData.entrySet()) {
            e.getKey().setSortOrder(order++);
            updateFileSortOrders(e.getValue());
        }
        assignAllBoxDocumentNames();
        assignAllBoxFileNames();
        treeView.refresh();
    }

    private void updateFileSortOrders(List<ScannedFile> files) {
        for (int i = 0; i < files.size(); i++) files.get(i).setSortOrder(i + 1);
    }

    private void updateDocumentFileCountLabels() {
        lblTotalFilesInDoc.setText(String.valueOf(currentFiles.size()));
    }

    private void updateTotalFilesInBoxLabel() {
        if (lblTotalFilesInBox != null)
            lblTotalFilesInBox.setText(
                    String.valueOf(sessionData.values().stream().mapToInt(List::size).sum()));
    }

    private void setTotalsVisible(boolean v) {
        lblTotalDocText.setVisible(v);    lblTotalDocText.setManaged(v);
        lblTotalDocInBox.setVisible(v);   lblTotalDocInBox.setManaged(v);
        lblTotalFilesText.setVisible(v);  lblTotalFilesText.setManaged(v);
        lblTotalFilesInDoc.setVisible(v); lblTotalFilesInDoc.setManaged(v);
    }

    public void setupPreview() {
        previewScrollPane.viewportBoundsProperty().addListener((obs, old, b) -> {
            previewImageView.setFitWidth(b.getWidth());
            previewImageView.setFitHeight(b.getHeight());
        });
    }

    private Path writeTempFile(String fileName, byte[] bytes) throws IOException {
        if (scanTempDir == null || !Files.exists(scanTempDir))
            scanTempDir = Files.createTempDirectory("tiffix-scans-");
        Path dest = scanTempDir.resolve(fileName);
        int suffix = 1; String base = fileName;
        while (Files.exists(dest)) {
            int dot = base.lastIndexOf('.');
            dest = scanTempDir.resolve(dot > 0
                    ? base.substring(0, dot) + "_" + suffix + base.substring(dot)
                    : base + "_" + suffix);
            suffix++;
        }
        Files.write(dest, bytes);
        return dest;
    }
}