package dk.easv.tiffixexamweblager.GUI.Controllers;

import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.ProfileCardController;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileRuleModel;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
import javafx.beans.property.SimpleBooleanProperty;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfilesTabController implements Initializable {

    @FXML private TableView<Profile> tblProfiles;
    @FXML private TableColumn<Profile, String> colTitle;
    @FXML private TableColumn<Profile, Boolean> colActive;
    @FXML private TableColumn<Profile, Void> colManage;

    // Create overlay — already wired
    @FXML private VBox profileCardOverlay;
    @FXML private ProfileCardController profileCardController;

    // Assign overlay — new
    @FXML private VBox assignEmployeeOverlay;
    @FXML private AssignEmployeeProfileController assignEmployeeProfileController;

    private ProfileRuleModel profileRuleModel;
    private UserModel userModel;

    public ProfilesTabController() {
        try {
            profileRuleModel = new ProfileRuleModel();
            userModel    = new UserModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to initialize models.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupActiveColumn();
        setupManageColumn();

        profileCardController.setOverlay(profileCardOverlay);
        profileCardController.setProfilesTabController(this);

        assignEmployeeProfileController.setOverlay(assignEmployeeOverlay);
    }

    private void setupTable() {
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));

        try {
            tblProfiles.setItems(profileRuleModel.getAllObservableIncludingSoftDeleted());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to retrieve profiles from database.");
        }

        tblProfiles.setRowFactory(tv -> {
            TableRow<Profile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showCreateOverlay();
                    profileCardController.preloadUpdateWindow(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupManageColumn() {
        colManage.setCellFactory(col -> new TableCell<>() {

            private final Button btnAssign = new Button();
            private final Button btnDelete = new Button();
            private final HBox container  = new HBox(8, btnAssign, btnDelete);

            {
                btnAssign.setGraphic(new FontIcon("bi-person-plus"));
                btnAssign.getStyleClass().add("icon-button");
                btnAssign.setOnAction(e ->
                        handleAssignEmployee(tblProfiles.getItems().get(getIndex()))
                );

                btnDelete.setGraphic(new FontIcon("bi-trash"));
                btnDelete.getStyleClass().addAll("icon-button", "danger");
                btnDelete.setOnAction(e ->
                        handleDeleteProfile(tblProfiles.getItems().get(getIndex()))
                );

                container.setAlignment(Pos.CENTER);
            }

            private void handleAssignEmployee(Profile profile) {
                if (profile == null) return;

                assignEmployeeProfileController.preload(userModel, profile.getId());
                showAssignOverlay();
            }

            private void handleDeleteProfile(Profile profile) {
                if (profile == null) return;

                boolean confirmed = AlertHelper.showConfirmation(
                        "Deactivate Profile",
                        "Are you sure you want to deactivate \"" + profile.getTitle() + "\"?"
                );
                if (!confirmed) return;

                try {
                    profileRuleModel.deleteProfile(profile);
                    tblProfiles.getItems().remove(profile);
                } catch (Exception e) {
                    AlertHelper.showError("Error", "Failed to deactivate profile.");
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
    private void handleCreateProfile(ActionEvent event) {
        showCreateOverlay();
        profileCardController.preloadCreateWindow();
    }

    public TableView<Profile> getTable() {
        return tblProfiles;
    }

    private void showCreateOverlay() {
        profileCardOverlay.setVisible(true);
        profileCardOverlay.setManaged(true);
    }

    private void showAssignOverlay() {
        assignEmployeeOverlay.setVisible(true);
        assignEmployeeOverlay.setManaged(true);
    }
}