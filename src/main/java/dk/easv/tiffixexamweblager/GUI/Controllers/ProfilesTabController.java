package dk.easv.tiffixexamweblager.GUI.Controllers;

import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.CreateProfileController;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileModel;
import dk.easv.tiffixexamweblager.GUI.Models.UserModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;
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
    @FXML private TableColumn<Profile, Void> colManage;

    // Create overlay — already wired
    @FXML private VBox createProfileOverlay;
    @FXML private CreateProfileController createProfileController;

    // Assign overlay — new
    @FXML private VBox assignEmployeeOverlay;
    @FXML private AssignEmployeeProfileController assignEmployeeProfileController;

    private ProfileModel profileModel;
    private UserModel userModel;

    public ProfilesTabController() {
        try {
            profileModel = new ProfileModel();
            userModel    = new UserModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to initialize models.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupManageColumn();

        createProfileController.setOverlay(createProfileOverlay);
        createProfileController.setProfilesTabController(this);

        assignEmployeeProfileController.setOverlay(assignEmployeeOverlay);
    }

    private void setupTable() {
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));

        try {
            tblProfiles.setItems(profileModel.getAllProfiles());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to retrieve profiles from database.");
        }

        tblProfiles.setRowFactory(tv -> {
            TableRow<Profile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showCreateOverlay();
                    createProfileController.preloadUpdateWindow(row.getItem());
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
                        "Delete Profile",
                        "Are you sure you want to delete \"" + profile.getTitle() + "\"?"
                );
                if (!confirmed) return;

                try {
                    profileModel.deleteProfile(profile);
                    tblProfiles.getItems().remove(profile);
                } catch (Exception e) {
                    AlertHelper.showError("Error", "Failed to delete profile.");
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
    private void handleCreateProfile(ActionEvent event) {
        showCreateOverlay();
        createProfileController.preloadCreateWindow();
    }

    public TableView<Profile> getTable() {
        return tblProfiles;
    }

    private void showCreateOverlay() {
        createProfileOverlay.setVisible(true);
        createProfileOverlay.setManaged(true);
    }

    private void showAssignOverlay() {
        assignEmployeeOverlay.setVisible(true);
        assignEmployeeOverlay.setManaged(true);
    }
}