package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.BoxCardController;
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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class BoxesTabController implements Initializable {

    @FXML private TableView<Box>            tblBoxes;
    @FXML private TableColumn<Box, Integer> colNumber;
    @FXML private TableColumn<Box, String>  colTitle;
    @FXML private TableColumn<Box, String>  colCreatedBy;
    @FXML private TableColumn<Box, String>  colCreatedAt;
    @FXML private TableColumn<Box, Integer> colDocuments;
    @FXML private TableColumn<Box, Integer> colPages;
    @FXML private TableColumn<Box, Boolean> colActive;
    @FXML private VBox              boxCardOverlay;
    @FXML private BoxCardController boxCardController;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private BoxDocumentModel boxDocumentModel;

    public BoxesTabController() {
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

        boxCardController.setOverlay(boxCardOverlay);
        boxCardController.setBoxesTabController(this);
        boxCardController.setBoxModel(boxDocumentModel);
        boxCardController.setLoggedInUser(
                UserSession.getInstance().getCurrentUser().getId(),
                UserSession.getInstance().getCurrentUser().getUsername()
        );
    }

    private void setupTable() {
        colNumber.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getNumber()));

        colTitle.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTitle()));

        colCreatedBy.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCreatedByUsername()));

        colCreatedAt.setCellValueFactory(d -> {
            LocalDateTime dt = d.getValue().getCreatedAt();
            return new SimpleStringProperty(dt != null ? dt.format(DATE_FMT) : "");
        });

        colDocuments.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getDocumentsAmount()));

        colPages.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getFilesAmount()));

        try {
            tblBoxes.setItems(boxDocumentModel.getAllObservableIncludingSoftDeleted());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to retrieve boxes from database.");
        }
    }

    private void setupActiveColumn() {
        colActive.setCellValueFactory(data ->
                new SimpleBooleanProperty(data.getValue().getIsDeleted())
        );

        colActive.setCellFactory(col -> new TableCell<>() {
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

    @FXML
    protected void handleCreate() {
        boxCardController.preloadCreateWindow();
        boxCardOverlay.setVisible(true);
        boxCardOverlay.setManaged(true);
    }
}