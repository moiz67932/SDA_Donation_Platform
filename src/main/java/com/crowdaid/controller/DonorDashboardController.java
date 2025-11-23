package com.crowdaid.controller;

import com.crowdaid.model.user.Donor;
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
 * Controller for the Donor Dashboard.
 * Main hub for donors to browse campaigns, view donations, and access the reward shop.
 */
public class DonorDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DonorDashboardController.class);
    
    private final ViewLoader viewLoader;
    private Donor currentDonor;
    
    @FXML private Label welcomeLabel;
    @FXML private Label creditBalanceLabel;
    @FXML private Button browseCampaignsButton;
    @FXML private Button myDonationsButton;
    @FXML private Button logoutButton;
    
    public DonorDashboardController() {
        this.viewLoader = ViewLoader.getInstance();
    }
    
    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        
        if (user == null || !(user instanceof Donor)) {
            AlertUtil.showError("Access Denied", "You must be logged in as a donor to access this page.");
            viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
            return;
        }
        
        currentDonor = (Donor) user;
        welcomeLabel.setText("Welcome, " + currentDonor.getName() + "!");
        
        // TODO: Load credit balance from database
        creditBalanceLabel.setText("Credits: 0");
        
        logger.info("Donor dashboard loaded for user: {}", currentDonor.getEmail());
    }
    
    /**
     * Navigate to browse campaigns screen (UC6).
     */
    @FXML
    private void handleBrowseCampaigns(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/browse_campaigns.fxml", "CrowdAid - Browse Campaigns");
    }
    
    /**
     * Navigate to my donations screen.
     */
    @FXML
    private void handleMyDonations(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/my_donations.fxml", "CrowdAid - My Donations");
    }
    
    /**
     * Handle logout.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().clear();
        logger.info("Donor logged out: {}", currentDonor.getEmail());
        viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
    }
}
