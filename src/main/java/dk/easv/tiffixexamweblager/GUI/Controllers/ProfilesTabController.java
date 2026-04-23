package dk.easv.tiffixexamweblager.GUI.Controllers;

// Project imports
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.GUI.Models.ProfileModel;

// Java imports
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfilesTabController implements Initializable {
    @FXML private TableView<Profile> tblProfiles;
    @FXML private TableColumn<Profile, String> colTitle;

    private ProfileModel profileModel;

    public ProfilesTabController() {
        try {
            profileModel = new ProfileModel();
        } catch (Exception e) {
            // Show Alert to user
            throw new RuntimeException(e);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
    }

    private void setupTable() {
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        try {
            tblProfiles.setItems(profileModel.getAllProfiles());
        } catch (Exception e) {
            // Show error to user
            throw new RuntimeException(e);
        }
    }


}
