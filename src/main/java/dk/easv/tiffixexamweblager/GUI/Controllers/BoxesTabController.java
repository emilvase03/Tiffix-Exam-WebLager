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
    @FXML private TableColumn<Box, Void>    colManage;
    @FXML private VBox              boxCardOverlay;
    @FXML private BoxCardController boxCardController;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private BoxDocumentModel boxDocumentModel;

    public BoxesTabController() {
        try {
            boxDocumentModel = new BoxDocumentModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to initialize Box model.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupActiveColumn();
        setupManageColumn();

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
                new SimpleObjectProperty<>(d.getValue().getPagesAmount()));

        try {
            tblBoxes.setItems(boxDocumentModel.getAllTrueObservableBoxes());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to retrieve boxes from database.");
        }
    }

    private void setupManageColumn() {
        colManage.setCellFactory(col -> new TableCell<>() {

            final Button btnEdit   = new Button();
            final Button btnDelete = new Button();
            final HBox   container = new HBox(8, btnEdit, btnDelete);
            {
                btnEdit.setGraphic(new FontIcon("bi-pencil"));
                btnEdit.getStyleClass().addAll("icon-button");
                btnEdit.setOnAction(e ->
                        handleEditBox(tblBoxes.getItems().get(getIndex()))
                );

                btnDelete.setGraphic(new FontIcon("bi-trash"));
                btnDelete.getStyleClass().addAll("icon-button", "danger");
                btnDelete.setOnAction(e ->
                        handleDeleteBox(tblBoxes.getItems().get(getIndex()))
                );
                container.setAlignment(Pos.CENTER);
            }

            private void handleEditBox(Box box) {
                if (box == null) return;
                boxCardController.preloadEditWindow(box);
                boxCardOverlay.setVisible(true);
                boxCardOverlay.setManaged(true);
            }

            private void handleDeleteBox(Box box) {
                if (box == null) return;

                boolean confirmed = AlertHelper.showConfirmation(
                        "Delete Box",
                        "Are you sure you want to delete box #" + box.getNumber()
                                + " \"" + box.getTitle() + "\"?"
                );
                if (!confirmed) return;

                try {
                    boxDocumentModel.deleteBox(box);
                } catch (Exception e) {
                    AlertHelper.showError("Error", "Failed to delete box.");
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
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
                    // Not implemented yet
                });

                btnDeactivate.setGraphic(new FontIcon("bi-dash-square"));
                btnDeactivate.getStyleClass().addAll("icon-button", "danger");
                btnDeactivate.setOnAction(e -> {
                    // Not implemented yet
                });

                container.setAlignment(Pos.CENTER);
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
    private void handleCreateBox() {
        boxCardController.preloadCreateWindow();
        boxCardOverlay.setVisible(true);
        boxCardOverlay.setManaged(true);
    }
}