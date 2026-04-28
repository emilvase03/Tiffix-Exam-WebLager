package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.CreateProfileController;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// AtlantaFX imports
import atlantafx.base.theme.Tweaks;

// Java imports
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfilesTabController implements Initializable {
    @FXML private TableView<Profile> tblProfiles;
    @FXML private TableColumn<Profile, String> colTitle;
    @FXML
    private TableColumn<Profile, Void> colManage;
    @FXML private VBox createProfileOverlay;
    @FXML private CreateProfileController createProfileController;
    private ProfileModel profileModel;


    public ProfilesTabController() {
        try {
            profileModel = new ProfileModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate ProfileModel.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupManageColumn();
        createProfileController.setOverlay(createProfileOverlay);
        createProfileController.setProfilesTabController(this);
    }

    private void setupTable() {
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        tblProfiles.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        try {
            tblProfiles.setItems(profileModel.getAllProfiles());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to retrieve profiles from database.");
        }

        tblProfiles.setRowFactory(tv -> {
            TableRow<Profile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Profile selectedProfile = row.getItem();
                    showOverlay();
                    createProfileController.preloadWindow(selectedProfile);
                }
            });
            return row;
        });
    }
    private void setupManageColumn() {
        colManage.setCellFactory(col -> new TableCell<>() {

            private final Button btnDelete = new Button();
            private final HBox container = new HBox(btnDelete);

            {
                FontIcon deleteIcon = new FontIcon("bi-trash");

                btnDelete.setGraphic(deleteIcon);
                btnDelete.getStyleClass().add("icon-button");
                btnDelete.getStyleClass().add("danger");

                btnDelete.setOnAction(e ->
                        handleDeleteProfile(getTableView().getItems().get(getIndex()))
                );

                container.setAlignment(Pos.CENTER);
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
        showOverlay();
    }

    public TableView<Profile> getTable() {
        return tblProfiles;
    }

    private void showOverlay() {
        createProfileOverlay.setVisible(true);
        createProfileOverlay.setManaged(true);
    }
}
