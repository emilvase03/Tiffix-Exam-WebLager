package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;
import dk.easv.tiffixexamweblager.BE.Profile;
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.GUI.Controllers.ProfilesTabController;
import dk.easv.tiffixexamweblager.GUI.Models.*;
import dk.easv.tiffixexamweblager.GUI.Utils.AlertHelper;

// ControlsFX imports
import org.controlsfx.control.CheckComboBox;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfileCardController implements Initializable {
    @FXML private TextField txtTitle;
    @FXML private Label lblHeader;
    @FXML private ComboBox<Customer> customerDropdown;
    @FXML private CheckComboBox<Rule> rulesDropdown;
    @FXML private ListView<Rule> rulesList;

    private VBox overlay;
    private ProfileModel profileModel;
    private ProfilesTabController profilesTabController;
    private Profile profileToBeUpdated;
    private RuleModel ruleModel;
    private ProfileRuleModel profileRuleModel;
    private CustomerModel customerModel;
    private CustomerProfileModel customerProfileModel;
    private boolean updateProfile = false;
    private ObservableList<Rule> allRules = FXCollections.observableArrayList();
    private ObservableList<Rule> profileRules = FXCollections.observableArrayList();

    public ProfileCardController() {
        try {
            profileModel = new ProfileModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate ProfileModel.");
        }
        try {
            ruleModel = new RuleModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate RuleModel");
        }
        try {
            profileRuleModel = new ProfileRuleModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate ProfileRuleModel");
        }
        try {
            customerModel = new CustomerModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate CustomerModel");
        }
        try {
            customerProfileModel = new CustomerProfileModel();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to instantiate CustomerProfileModel");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListeners();
    }

    private void setupOverLay() {
        rulesDropdown.getItems().clear();
        rulesList.getItems().clear();
        profileRules.clear();
        allRules.clear();

        try {
            allRules.addAll(ruleModel.getAllRules());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to retrieve every profile rule from database");
        }

        try {
            customerDropdown.getItems().addAll(customerModel.getAllCustomers());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to retrieve customers from database");
        }

        // If update vs If create
        if (updateProfile) {
            try {
                customerDropdown.setValue(customerProfileModel.getCustomerForProfile(profileToBeUpdated));
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to retrieve customer for profile from database");
            }
            try {
                profileRules.addAll(ruleModel.getRulesForProfile(profileToBeUpdated));
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to retrieve profile rules from database");
            }

            rulesDropdown.getItems().addAll(allRules);
            for (Rule r : profileRules) {
                rulesDropdown.getCheckModel().check(r);
            }

        } else {
            rulesDropdown.getItems().addAll(allRules);
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (txtTitle.getText().isBlank()) {
            txtTitle.setStyle("-fx-border-color: #FF3D32; -fx-border-width: 1;");
            return;
        }
        if (customerDropdown.getValue() == null) {
            customerDropdown.setStyle("-fx-border-color: #FF3D32;");
            return;
        }
        if (rulesList.getItems().isEmpty()) {
            rulesList.setStyle("-fx-border-color: #FF3D32;");
            return;
        }

        // If update vs If create
        if (updateProfile) {
            try {
                if (profileToBeUpdated != null) {
                    profileToBeUpdated.setTitle(txtTitle.getText().trim());

                    profileModel.updateProfile(profileToBeUpdated);
                    profileRuleModel.updateRulesForProfile(profileToBeUpdated, rulesList.getItems());
                    customerProfileModel.updateProfileForCustomer(customerDropdown.getSelectionModel().getSelectedItem(), profileToBeUpdated);
                    profilesTabController.getTable().refresh();

                    txtTitle.clear();
                    lblHeader.setText("Create a new profile.");
                    handleClose();
                    updateProfile = false;
                }
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to update profile.");
            }
        } else {
            try {
                Profile profile = profileModel.createProfile(new Profile(txtTitle.getText().trim()));
                profilesTabController.getTable().getItems().add(profile);

                profileRuleModel.addRulesToProfile(profile, rulesList.getItems());
                customerProfileModel.addProfileToCustomer(customerDropdown.getSelectionModel().getSelectedItem(), profile);

                txtTitle.clear();
                handleClose();
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to create new profile.");
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        handleClose();
    }

    private void setupListeners() {
        rulesDropdown.getCheckModel().getCheckedItems().addListener((ListChangeListener<Rule>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    rulesList.getItems().addAll(change.getAddedSubList());
                }
                if (change.wasRemoved()) {
                    rulesList.getItems().removeAll(change.getRemoved());
                }
            }
        } );
    }

    public void setOverlay(VBox overlay) {
        this.overlay = overlay;
    }

    public void setProfilesTabController(ProfilesTabController profilesTabController) {
        this.profilesTabController = profilesTabController;
    }

    public void preloadUpdateWindow(Profile profile) {
        txtTitle.setText(profile.getTitle());
        lblHeader.setText("Edit profile.");
        updateProfile = true;
        profileToBeUpdated = profile;
        setupOverLay();
    }

    public void preloadCreateWindow() {
        lblHeader.setText("Create a new profile.");
        updateProfile = false;
        setupOverLay();
    }

    private void handleClose() {
        if (overlay != null) {
            updateProfile = false;
            txtTitle.clear();
            txtTitle.setStyle("");
            rulesList.setStyle("");
            customerDropdown.getItems().clear();
            customerDropdown.setValue(null);
            overlay.setVisible(false);
            overlay.setManaged(false);
        }
    }
}
