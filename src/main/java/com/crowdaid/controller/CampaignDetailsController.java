package com.crowdaid.controller;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.user.Donor;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Controller for viewing campaign details.
 * Allows donors to see full campaign information and make donations.
 */
public class CampaignDetailsController {
    
    private static final Logger logger = LoggerFactory.getLogger(CampaignDetailsController.class);
    
    private final ViewLoader viewLoader;
    private final CampaignRepository campaignRepository;
    private Campaign currentCampaign;
    private Donor currentDonor;
    
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label campaignerLabel;
    @FXML private Label goalLabel;
    @FXML private Label collectedLabel;
    @FXML private Label progressLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Button donateButton;
    @FXML private Button backButton;
    
    public CampaignDetailsController() {
        this.viewLoader = ViewLoader.getInstance();
        this.campaignRepository = new MySQLCampaignRepository();
    }
    
    @FXML
    private void initialize() {
        // Get campaign from session
        currentCampaign = (Campaign) SessionManager.getInstance().getAttribute("selectedCampaign");
        
        if (currentCampaign != null) {
            displayCampaignDetails();
            logger.info("Campaign details loaded: {}", currentCampaign.getTitle());
        } else {
            AlertUtil.showError("Error", "No campaign selected.");
            handleBack(null);
        }
        
        // Check if current user is a donor
        if (SessionManager.getInstance().getCurrentUser() instanceof Donor) {
            currentDonor = (Donor) SessionManager.getInstance().getCurrentUser();
        }
    }
    
    /**
     * Set the campaign to display.
     */
    public void setCampaign(Campaign campaign) {
        this.currentCampaign = campaign;
        displayCampaignDetails();
    }
    
    /**
     * Display campaign details in UI.
     */
    private void displayCampaignDetails() {
        if (currentCampaign == null) return;
        
        titleLabel.setText(currentCampaign.getTitle());
        categoryLabel.setText("Category: " + currentCampaign.getCategory().name());
        campaignerLabel.setText("Campaigner ID: " + currentCampaign.getCampaignerId());
        goalLabel.setText(String.format("Goal: $%.2f", currentCampaign.getGoalAmount()));
        collectedLabel.setText(String.format("Raised: $%.2f", currentCampaign.getCollectedAmount()));
        
        // Calculate progress percentage
        double progress = (currentCampaign.getCollectedAmount() / currentCampaign.getGoalAmount()) * 100;
        if (progressLabel != null) {
            progressLabel.setText(String.format("Progress: %.1f%%", progress));
        }
        
        statusLabel.setText("Status: " + currentCampaign.getStatus().name());
        descriptionArea.setText(currentCampaign.getDescription());
    }
    
    /**
     * Handle donate button click (UC7).
     */
    @FXML
    private void handleDonate(ActionEvent event) {
        if (currentDonor == null) {
            AlertUtil.showError("Access Denied", "You must be logged in as a donor to make donations.");
            return;
        }
        
        if (currentCampaign == null) {
            AlertUtil.showError("Error", "No campaign selected.");
            return;
        }
        
        // Store campaign in session for donation dialog
        SessionManager.getInstance().setAttribute("selectedCampaign", currentCampaign);
        
        // Open donation dialog
        try {
            viewLoader.loadDialog("/fxml/donation_dialog.fxml", "Make a Donation");
            
            // Reload campaign data after donation
            Campaign refreshed = campaignRepository.findById(currentCampaign.getId());
            if (refreshed != null) {
                currentCampaign = refreshed;
                displayCampaignDetails();
            }
        } catch (SQLException e) {
            logger.error("Error refreshing campaign data", e);
        }
    }
    
    /**
     * Handle back button click.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/browse_campaigns.fxml", "CrowdAid - Browse Campaigns");
    }
}
