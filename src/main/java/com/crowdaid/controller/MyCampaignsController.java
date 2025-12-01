package com.crowdaid.controller;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.CampaignStatus;
import com.crowdaid.model.user.Campaigner;
import com.crowdaid.model.user.User;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class MyCampaignsController {
    
    private static final Logger logger = LoggerFactory.getLogger(MyCampaignsController.class);
    
    private final ViewLoader viewLoader;
    private final MySQLCampaignRepository campaignRepository;
    private Campaigner currentCampaigner;
    
    @FXML private TableView<Campaign> campaignsTable;
    @FXML private TableColumn<Campaign, String> titleColumn;
    @FXML private TableColumn<Campaign, String> categoryColumn;
    @FXML private TableColumn<Campaign, Double> goalColumn;
    @FXML private TableColumn<Campaign, Double> collectedColumn;
    @FXML private TableColumn<Campaign, CampaignStatus> statusColumn;
    @FXML private TableColumn<Campaign, String> startDateColumn;
    @FXML private TableColumn<Campaign, String> endDateColumn;
    @FXML private Button backButton;
    @FXML private Button viewDetailsButton;
    @FXML private Button editButton;
    @FXML private Button manageMilestonesButton;
    @FXML private Button manageSubscriptionTiersButton;
    @FXML private Button requestFundsButton;
    @FXML private Button logoutButton;
    
    public MyCampaignsController() {
        this.viewLoader = ViewLoader.getInstance();
        this.campaignRepository = new MySQLCampaignRepository();
    }
    
    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        
        if (user == null || !(user instanceof Campaigner)) {
            AlertUtil.showError("Access Denied", "You must be logged in as a campaigner.");
            viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
            return;
        }
        
        currentCampaigner = (Campaigner) user;
        
        // Setup table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        goalColumn.setCellValueFactory(new PropertyValueFactory<>("goalAmount"));
        collectedColumn.setCellValueFactory(new PropertyValueFactory<>("collectedAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        
        // Custom cell factory for status column to show approval state
        statusColumn.setCellFactory(column -> new TableCell<Campaign, CampaignStatus>() {
            @Override
            protected void updateItem(CampaignStatus status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    CampaignStatus campaignStatus = status;
                    
                    HBox statusBox = new HBox(5);
                    statusBox.setAlignment(Pos.CENTER_LEFT);
                    
                    Label statusLabel = new Label();
                    
                    if (campaignStatus == CampaignStatus.PENDING_REVIEW) {
                        statusLabel.setText("⏳ NOT APPROVED");
                        statusLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                    } else if (campaignStatus == CampaignStatus.ACTIVE) {
                        statusLabel.setText("✓ APPROVED");
                        statusLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                    } else if (campaignStatus == CampaignStatus.REJECTED) {
                        statusLabel.setText("✗ REJECTED");
                        statusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    } else {
                        statusLabel.setText(status.name());
                    }
                    
                    statusBox.getChildren().add(statusLabel);
                    setGraphic(statusBox);
                    setText(null);
                }
            }
        });
        
        loadCampaigns();
    }
    
    private void loadCampaigns() {
        try {
            List<Campaign> campaigns = campaignRepository.findByCampaignerId(currentCampaigner.getId());
            ObservableList<Campaign> campaignList = FXCollections.observableArrayList(campaigns);
            campaignsTable.setItems(campaignList);
            logger.info("Loaded {} campaigns for campaigner: {}", campaigns.size(), currentCampaigner.getEmail());
        } catch (SQLException e) {
            logger.error("Error loading campaigns", e);
            AlertUtil.showError("Database Error", "Failed to load campaigns: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewDetails(ActionEvent event) {
        Campaign selected = campaignsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a campaign to view details.");
            return;
        }
        
        SessionManager.getInstance().setAttribute("selectedCampaign", selected);
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/campaign_details.fxml", "CrowdAid - Campaign Details");
    }
    
    @FXML
    private void handleEdit(ActionEvent event) {
        Campaign selected = campaignsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a campaign to edit.");
            return;
        }
        
        AlertUtil.showInfo("Unavailable", "Campaign editing is not currently available. Please create a new campaign if you need to make changes.");
    }
    
    @FXML
    private void handleManageMilestones(ActionEvent event) {
        Campaign selected = campaignsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a campaign to manage milestones.");
            return;
        }
        
        SessionManager.getInstance().setAttribute("selectedCampaign", selected);
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/milestone_management.fxml", "CrowdAid - Manage Milestones");
    }
    
    /**
     * Handle manage subscription tiers button click.
     */
    @FXML
    private void handleManageSubscriptionTiers(ActionEvent event) {
        Campaign selected = campaignsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a campaign to manage subscription tiers.");
            return;
        }
        
        try {
            // Load manage subscription tiers dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manage_subscription_tiers.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Get controller and set campaign
            ManageSubscriptionTiersController controller = loader.getController();
            controller.setCampaign(selected);
            
            // Create and show dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Manage Subscription Tiers");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            logger.error("Error opening manage subscription tiers dialog", e);
            AlertUtil.showError("Error", "Failed to open subscription tier management: " + e.getMessage());
        }
    }
    
    /**
     * Handle request fund release - triggers milestone voting for escrow campaigns.
     */
    @FXML
    private void handleRequestFundRelease(ActionEvent event) {
        Campaign selected = campaignsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a campaign to request fund release.");
            return;
        }
        
        // Check if campaign uses escrow
        if (!selected.isEscrowEnabled()) {
            AlertUtil.showInfo("Not Applicable", 
                "This campaign does not use escrow. Funds are released automatically upon donation.");
            return;
        }
        
        // Check if campaign is active
        if (selected.getStatus() != CampaignStatus.ACTIVE) {
            AlertUtil.showWarning("Campaign Not Active", 
                "Funds can only be requested for active campaigns.");
            return;
        }
        
        // Navigate to milestone management where they can submit milestones for voting
        AlertUtil.showInfo("Request Fund Release", 
            "To release escrow funds, you need to submit a completed milestone for voting. \n\n" +
            "Navigate to Milestone Management and mark milestones as completed, then submit them for donor voting. \n\n" +
            "Once 60% of donors approve, the funds will be released.");
        
        SessionManager.getInstance().setAttribute("selectedCampaign", selected);
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/milestone_management.fxml", "CrowdAid - Manage Milestones");
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/campaigner_dashboard.fxml", "CrowdAid - Campaigner Dashboard");
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().clear();
        logger.info("User logged out: {}", currentCampaigner.getEmail());
        viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
    }
}
