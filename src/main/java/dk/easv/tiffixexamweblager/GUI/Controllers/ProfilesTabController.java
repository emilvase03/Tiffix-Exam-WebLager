package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import atlantafx.base.theme.Tweaks;
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.GUI.Controllers.components.CreateProfileController;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileModel;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// Java imports
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfilesTabController implements Initializable {
    @FXML private TableView<Profile> tblProfiles;
    @FXML private TableColumn<Profile, String> colTitle;
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
        System.out.println(createProfileOverlay);
        System.out.println(createProfileController);
        setupTable();
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
    }

    @FXML
    private void handleCreateProfile(ActionEvent event) {
        createProfileOverlay.setVisible(true);
        createProfileOverlay.setManaged(true);
    }

    public TableView<Profile> getTable() {
        return tblProfiles;
    }
}
