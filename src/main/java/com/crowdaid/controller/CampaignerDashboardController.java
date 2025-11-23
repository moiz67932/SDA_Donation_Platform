package com.crowdaid.controller;

import com.crowdaid.model.user.Campaigner;
import com.crowdaid.model.user.User;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Campaigner Dashboard.
 * Main hub for campaigners to manage campaigns and milestones.
 */
public class CampaignerDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(CampaignerDashboardController.class);
    
    private final ViewLoader viewLoader;
    private Campaigner currentCampaigner;
    
    @FXML private Label welcomeLabel;
    @FXML private Label statsLabel;
    @FXML private Button createCampaignButton;
    @FXML private Button myCampaignsButton;
    @FXML private Button logoutButton;
    
    public CampaignerDashboardController() {
        this.viewLoader = ViewLoader.getInstance();
    }
    
    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        
        if (user == null || !(user instanceof Campaigner)) {
            AlertUtil.showError("Access Denied", "You must be logged in as a campaigner to access this page.");
            viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
            return;
        }
        
        currentCampaigner = (Campaigner) user;
        welcomeLabel.setText("Welcome, " + currentCampaigner.getName() + "!");
        
        // TODO: Load campaign statistics from database
        statsLabel.setText("Active Campaigns: 0 | Total Raised: $0.00");
        
        logger.info("Campaigner dashboard loaded for user: {}", currentCampaigner.getEmail());
    }
    
    /**
     * Navigate to create campaign screen (UC3).
     */
    @FXML
    private void handleCreateCampaign(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/create_campaign.fxml", "CrowdAid - Create Campaign");
    }
    
    /**
     * Navigate to my campaigns screen.
     */
    @FXML
    private void handleMyCampaigns(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/my_campaigns.fxml", "CrowdAid - My Campaigns");
    }
    
    /**
     * Handle logout.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().clear();
        logger.info("Campaigner logged out: {}", currentCampaigner.getEmail());
        viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
    }
}
