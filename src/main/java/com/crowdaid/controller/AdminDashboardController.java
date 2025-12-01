package com.crowdaid.controller;

import com.crowdaid.model.user.Administrator;
import com.crowdaid.model.user.User;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.interfaces.DonationRepository;
import com.crowdaid.repository.interfaces.UserRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.repository.mysql.MySQLDonationRepository;
import com.crowdaid.repository.mysql.MySQLUserRepository;
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
 * Controller for the Administrator Dashboard.
 * Main hub for admins to approve campaigns and manage the reward shop.
 */
public class AdminDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    
    private final ViewLoader viewLoader;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final DonationRepository donationRepository;
    private Administrator currentAdmin;
    
    @FXML private Label welcomeLabel;
    @FXML private Label statsLabel;
    @FXML private Button approveCampaignsButton;
    @FXML private Button manageRewardsButton;
    @FXML private Button logoutButton;
    
    public AdminDashboardController() {
        this.viewLoader = ViewLoader.getInstance();
        this.campaignRepository = new MySQLCampaignRepository();
        this.userRepository = new MySQLUserRepository();
        this.donationRepository = new MySQLDonationRepository();
    }
    
    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        
        if (user == null || !(user instanceof Administrator)) {
            AlertUtil.showError("Access Denied", "You must be logged in as an administrator to access this page.");
            viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
            return;
        }
        
        currentAdmin = (Administrator) user;
        welcomeLabel.setText("Welcome, " + currentAdmin.getName() + "!");
        
        loadStatistics();
        
        logger.info("Admin dashboard loaded for user: {}", currentAdmin.getEmail());
    }
    
    /**
     * Navigate to campaign approval screen (UC11).
     */
    @FXML
    private void handleApproveCampaigns(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/campaign_approval.fxml", "CrowdAid - Approve Campaigns");
    }
    
    /**
     * Navigate to reward management screen (UC12).
     */
    @FXML
    private void handleManageRewards(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/reward_management.fxml", "CrowdAid - Manage Rewards");
    }
    
    /**
     * Handle logout.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().clear();
        logger.info("Admin logged out: {}", currentAdmin.getEmail());
        viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
    }
    
    /**
     * Load dashboard statistics.
     */
    private void loadStatistics() {
        try {
            int pendingCount = campaignRepository.countByStatus(com.crowdaid.model.campaign.CampaignStatus.PENDING_REVIEW);
            int totalUsers = userRepository.countAll();
            double totalDonations = donationRepository.getTotalDonationAmount();
            
            statsLabel.setText(String.format("Pending Campaigns: %d | Total Users: %d | Total Donations: $%.2f",
                                           pendingCount, totalUsers, totalDonations));
            
            logger.debug("Admin statistics loaded: pending={}, users={}, donations=${}",
                        pendingCount, totalUsers, totalDonations);
        } catch (Exception e) {
            logger.error("Error loading admin statistics", e);
            statsLabel.setText("Statistics unavailable");
        }
    }
}
