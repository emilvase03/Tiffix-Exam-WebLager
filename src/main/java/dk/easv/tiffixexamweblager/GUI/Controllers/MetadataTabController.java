package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.MetadataCardController;
import dk.easv.tiffixexamweblager.GUI.Models.BoxDocumentModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// Ikonli imports
import org.kordamp.ikonli.javafx.FontIcon;

// Java imports
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class MetadataTabController implements Initializable {
    @FXML private TableView<Box> tblMetadata;
    @FXML private TableColumn<Box, String> colBoxTitle;
    @FXML private TableColumn<Box, String> colNotes;
    @FXML private TableColumn<Box, Integer> colDocumentsAmount;
    @FXML private TableColumn<Box, Integer> colFilesAmount;
    @FXML private TableColumn<Box, Boolean> colBoxActiveStatus;
    @FXML private VBox metadataCardOverlay;
    @FXML private MetadataCardController metadataCardController;

    private BoxDocumentModel boxDocumentModel;

    public MetadataTabController() {
        try {
            boxDocumentModel = new BoxDocumentModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to initialize box model.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupActiveColumn();

        metadataCardController.init();
        metadataCardController.setOverlay(metadataCardOverlay);
        metadataCardController.setMetadataTabController(this);
    }

    private void setupTable() {
        colBoxTitle.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getTitle()));

        colNotes.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNotes()));

        colDocumentsAmount.setCellValueFactory(d ->
                new SimpleObjectProperty(d.getValue().getDocumentsAmount()));

        colFilesAmount.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getFilesAmount()));

        try {
            tblMetadata.setItems(boxDocumentModel.getAllObservableIncludingSoftDeleted());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to retrieve metadata from database.");
        }

        tblMetadata.setRowFactory(tv -> {
            TableRow<Box> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showOverlay();
                    metadataCardController.preloadWindow(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupActiveColumn() {
        colBoxActiveStatus.setCellValueFactory(data ->
                new SimpleBooleanProperty(data.getValue().getIsDeleted())
        );

        colBoxActiveStatus.setCellFactory(col -> new TableCell<>() {
            final Button btnActive = new Button();
            final Button btnDeactivate = new Button();
            final HBox container = new HBox();
            {
                btnActive.setGraphic(new FontIcon("bi-check-square"));
                btnActive.getStyleClass().addAll("icon-button");
                btnActive.setOnAction(e -> {
                    handleToggle(true);
                });

                btnDeactivate.setGraphic(new FontIcon("bi-dash-square"));
                btnDeactivate.getStyleClass().addAll("icon-button", "danger");
                btnDeactivate.setOnAction(e -> {
                    handleToggle(false);
                });

                container.setAlignment(Pos.CENTER);
            }

            private void handleToggle(boolean newDeletedState) {
                Box box = getTableView().getItems().get(getIndex());
                boolean success = false;
                try {
                    success = boxDocumentModel.toggleSoftDelete(box.getId());
                } catch (Exception ex) {
                    AlertHelper.showError("Error", "Failed to toggle active status of " +box.getTitle());
                }

                if (success) {
                    box.setIsDeleted(newDeletedState);
                    getTableView().refresh();
                }
            }

            @Override
            protected void updateItem(Boolean isDeleted, boolean empty) {
                super.updateItem(isDeleted, empty);
                if (empty || isDeleted == null) {
                    setGraphic(null);
                    return;
                }
                container.getChildren().setAll(isDeleted ? btnDeactivate : btnActive);
                setGraphic(container);
            }
        });
    }

    private void showOverlay() {
        metadataCardOverlay.setVisible(true);
        metadataCardOverlay.setManaged(true);
    }

    public TableView getTable() {
        return tblMetadata;
    }
}
