package com.crowdaid.controller;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.CampaignCategory;
import com.crowdaid.model.campaign.CampaignStatus;
import com.crowdaid.model.user.Campaigner;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Controller for creating campaigns (UC3: Create Fundraising Campaign).
 */
public class CreateCampaignController {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateCampaignController.class);
    
    private final ViewLoader viewLoader;
    private final CampaignRepository campaignRepository;
    private Campaigner campaigner;
    
    @FXML private TextField titleField;
    @FXML private ComboBox<CampaignCategory> categoryComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField goalAmountField;
    @FXML private DatePicker endDatePicker;
    @FXML private CheckBox escrowEnabledCheckBox;
    @FXML private Button createButton;
    @FXML private Button cancelButton;
    
    private Campaign createdCampaign; // Store created campaign for tier management
    
    public CreateCampaignController() {
        this.viewLoader = ViewLoader.getInstance();
        this.campaignRepository = new MySQLCampaignRepository();
    }
    
    @FXML
    private void initialize() {
        campaigner = (Campaigner) SessionManager.getInstance().getCurrentUser();
        
        // Setup category combo box
        categoryComboBox.getItems().addAll(CampaignCategory.values());
        
        logger.info("Create campaign screen initialized");
    }
    
    /**
     * Handle create button click.
     */
    @FXML
    private void handleCreate(ActionEvent event) {
        String title = titleField.getText();
        CampaignCategory category = categoryComboBox.getValue();
        String description = descriptionArea.getText();
        String goalAmountStr = goalAmountField.getText();
        LocalDate endDate = endDatePicker.getValue();
        
        // Basic validation
        if (title == null || title.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Title is required.");
            return;
        }
        
        if (category == null) {
            AlertUtil.showError("Validation Error", "Category is required.");
            return;
        }
        
        if (description == null || description.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Description is required.");
            return;
        }
        
        if (goalAmountStr == null || goalAmountStr.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Goal amount is required.");
            return;
        }
        
        try {
            double goalAmount = Double.parseDouble(goalAmountStr);
            
            if (goalAmount <= 0) {
                AlertUtil.showError("Validation Error", "Goal amount must be positive.");
                return;
            }
            
            // Validate end date if provided
            if (endDate != null && endDate.isBefore(LocalDate.now())) {
                AlertUtil.showError("Validation Error", "End date must be in the future.");
                return;
            }
            
            // Create campaign object
            Campaign campaign = new Campaign(campaigner.getId(), title, description, goalAmount, category);
            campaign.setEndDate(endDate);
            campaign.setStartDate(LocalDate.now());
            campaign.setStatus(CampaignStatus.PENDING_REVIEW);
            campaign.setEscrowEnabled(escrowEnabledCheckBox.isSelected());
            
            // Save campaign to database
            Campaign savedCampaign = campaignRepository.save(campaign);
            
            if (savedCampaign != null && savedCampaign.getId() != null) {
                logger.info("Campaign created successfully: id={}, title={}, campaigner={}", 
                           savedCampaign.getId(), savedCampaign.getTitle(), campaigner.getEmail());
                
                createdCampaign = savedCampaign;
                
                // Ask if user wants to add subscription tiers
                Alert confirmTiers = new Alert(Alert.AlertType.CONFIRMATION);
                confirmTiers.setTitle("Campaign Created Successfully");
                confirmTiers.setHeaderText("Your campaign has been created and is pending admin approval.");
                confirmTiers.setContentText("Would you like to add subscription tiers now? " +
                        "This allows donors to subscribe with recurring monthly payments.\n\n" +
                        "You can also add tiers later from 'My Campaigns'.");
                
                ButtonType yesButton = new ButtonType("Add Tiers Now", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType("Skip for Now", ButtonBar.ButtonData.NO);
                confirmTiers.getButtonTypes().setAll(yesButton, noButton);
                
                confirmTiers.showAndWait().ifPresent(response -> {
                    if (response == yesButton) {
                        openTierManagementDialog(savedCampaign);
                    } else {
                        AlertUtil.showSuccess("Campaign Created", 
                            "Your campaign has been created successfully and is pending admin approval.");
                        viewLoader.loadView(viewLoader.getPrimaryStage(), 
                            "/fxml/campaigner_dashboard.fxml", "CrowdAid - Campaigner Dashboard");
                    }
                });
            } else {
                AlertUtil.showError("Error", "Failed to create campaign. Please try again.");
                logger.error("Failed to create campaign: savedCampaign was null or had no ID");
            }
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Please enter a valid goal amount.");
            logger.warn("Invalid goal amount format: {}", goalAmountStr);
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to save campaign. Please try again.");
            logger.error("Database error while creating campaign", e);
        } catch (Exception e) {
            AlertUtil.showError("Error", "An unexpected error occurred. Please try again.");
            logger.error("Unexpected error while creating campaign", e);
        }
    }
    
    /**
     * Handle cancel button click.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/campaigner_dashboard.fxml", "CrowdAid - Campaigner Dashboard");
    }
    
    /**
     * Opens tier management dialog for the created campaign.
     */
    private void openTierManagementDialog(Campaign campaign) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manage_subscription_tiers.fxml"));
            Scene scene = new Scene(loader.load());
            
            ManageSubscriptionTiersController controller = loader.getController();
            controller.setCampaign(campaign);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Subscription Tiers - " + campaign.getTitle());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(scene);
            dialogStage.setOnHidden(e -> {
                // Return to dashboard after closing tier dialog
                viewLoader.loadView(viewLoader.getPrimaryStage(), 
                    "/fxml/campaigner_dashboard.fxml", "CrowdAid - Campaigner Dashboard");
            });
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            logger.error("Error opening tier management dialog", e);
            AlertUtil.showError("Error", "Failed to open tier management: " + e.getMessage());
            viewLoader.loadView(viewLoader.getPrimaryStage(), 
                "/fxml/campaigner_dashboard.fxml", "CrowdAid - Campaigner Dashboard");
        }
    }
}
