package dk.easv.tiffixexamweblager.GUI.Controllers;

import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.BoxCardController;
import dk.easv.tiffixexamweblager.GUI.Models.BoxModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import org.kordamp.ikonli.javafx.FontIcon;
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
    @FXML private TableColumn<Box, Void>    colManage;

    @FXML private VBox              boxCardOverlay;
    @FXML private BoxCardController boxCardController;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private BoxModel boxModel;

    public BoxesTabController() {
        try {
            boxModel = new BoxModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to initialize Box model.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupManageColumn();

        boxCardController.setOverlay(boxCardOverlay);
        boxCardController.setBoxesTabController(this);
        boxCardController.setBoxModel(boxModel);
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
            tblBoxes.setItems(boxModel.getAllBoxes());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to retrieve boxes from database.");
        }
    }

    private void setupManageColumn() {
        colManage.setCellFactory(col -> new TableCell<>() {

            private final Button btnDelete = new Button();
            private final HBox   container = new HBox(8, btnDelete);

            {
                btnDelete.setGraphic(new FontIcon("bi-trash"));
                btnDelete.getStyleClass().addAll("icon-button", "danger");
                btnDelete.setOnAction(e ->
                        handleDeleteBox(tblBoxes.getItems().get(getIndex()))
                );
                container.setAlignment(Pos.CENTER);
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
                    boxModel.deleteBox(box);
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

    @FXML
    private void handleCreateBox() {
        boxCardController.preloadCreateWindow();
        boxCardOverlay.setVisible(true);
        boxCardOverlay.setManaged(true);
    }

    public TableView<Box> getTable() {
        return tblBoxes;
    }
}