package com.crowdaid.controller;

import com.crowdaid.model.user.Administrator;
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
 * Controller for the Administrator Dashboard.
 * Main hub for admins to approve campaigns and manage the reward shop.
 */
public class AdminDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    
    private final ViewLoader viewLoader;
    private Administrator currentAdmin;
    
    @FXML private Label welcomeLabel;
    @FXML private Label statsLabel;
    @FXML private Button approveCampaignsButton;
    @FXML private Button logoutButton;
    
    public AdminDashboardController() {
        this.viewLoader = ViewLoader.getInstance();
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
        
        // TODO: Load system statistics from database
        statsLabel.setText("Pending Campaigns: 0 | Total Users: 0 | Total Donations: $0.00");
        
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
     * Handle logout.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().clear();
        logger.info("Admin logged out: {}", currentAdmin.getEmail());
        viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
    }
}
