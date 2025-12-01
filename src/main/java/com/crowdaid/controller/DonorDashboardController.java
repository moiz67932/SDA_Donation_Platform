package com.crowdaid.controller;

import com.crowdaid.model.user.Donor;
import com.crowdaid.model.user.User;
import com.crowdaid.service.CreditService;
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
    private final CreditService creditService;
    private Donor currentDonor;
    
    @FXML private Label welcomeLabel;
    @FXML private Label creditBalanceLabel;
    @FXML private Button browseCampaignsButton;
    @FXML private Button myDonationsButton;
    @FXML private Button logoutButton;
    
    public DonorDashboardController() {
        this.viewLoader = ViewLoader.getInstance();
        this.creditService = new CreditService();
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
        
        loadCreditBalance();
        
        // Check if credits should be refreshed (after donation)
        Boolean refreshCredits = (Boolean) SessionManager.getInstance().getAttribute("refreshCredits");
        if (refreshCredits != null && refreshCredits) {
            // Refresh credit balance
            loadCreditBalance();
            SessionManager.getInstance().removeAttribute("refreshCredits");
        }
        
        logger.info("Donor dashboard loaded for user: {}", currentDonor.getEmail());
    }
    
    /**
     * Load and display the donor's credit balance.
     */
    private void loadCreditBalance() {
        try {
            int credits = creditService.getCreditBalance(currentDonor.getId());
            creditBalanceLabel.setText("Credits: " + credits);
            creditBalanceLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;"); // Green color
            logger.debug("Credit balance loaded: {}", credits);
        } catch (Exception e) {
            logger.error("Error loading credit balance", e);
            creditBalanceLabel.setText("Credits: 0");
        }
    }
    
    /**
     * Public method to refresh credit balance (can be called from other controllers).
     */
    public void refreshCreditBalance() {
        loadCreditBalance();
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
     * Navigate to my subscriptions screen (UC8).
     */
    @FXML
    private void handleMySubscriptions(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/my_subscriptions.fxml", "CrowdAid - My Subscriptions");
    }
    
    /**
     * Navigate to reward shop screen (UC10).
     */
    @FXML
    private void handleRewardShop(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/reward_shop.fxml", "CrowdAid - Reward Shop");
    }
    
    /**
     * Navigate to voting requests screen (UC9).
     */
    @FXML
    private void handleVotingRequests(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/voting_requests.fxml", "CrowdAid - Voting Requests");
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
