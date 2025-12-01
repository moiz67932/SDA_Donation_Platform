package com.crowdaid.controller;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.donation.SubscriptionTier;
import com.crowdaid.model.user.User;
import com.crowdaid.service.SubscriptionService;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller for subscription dialog (UC8: Subscribe to Campaign).
 */
public class SubscriptionDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionDialogController.class);
    
    private Campaign campaign;
    private SubscriptionService subscriptionService;
    private List<SubscriptionTier> availableTiers;
    
    @FXML private Label campaignTitleLabel;
    @FXML private ListView<SubscriptionTier> tierListView;
    @FXML private TextArea benefitsTextArea;
    @FXML private Label monthlyAmountLabel;
    @FXML private Button subscribeButton;
    @FXML private Button cancelButton;
    
    @FXML
    private void initialize() {
        subscriptionService = new SubscriptionService();
        
        // Configure tier list view
        tierListView.setCellFactory(param -> new ListCell<SubscriptionTier>() {
            @Override
            protected void updateItem(SubscriptionTier tier, boolean empty) {
                super.updateItem(tier, empty);
                if (empty || tier == null) {
                    setText(null);
                } else {
                    setText(tier.getDisplayText());
                }
            }
        });
        
        // Add selection listener
        tierListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    updateTierDetails(newValue);
                }
            }
        );
        
        logger.info("Subscription dialog initialized");
    }
    
    /**
     * Set the campaign for subscription and load tiers.
     */
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
        campaignTitleLabel.setText(campaign.getTitle());
        loadSubscriptionTiers();
    }
    
    /**
     * Load subscription tiers for the campaign.
     */
    private void loadSubscriptionTiers() {
        try {
            availableTiers = subscriptionService.getTiersByCampaign(campaign.getId());
            
            if (availableTiers.isEmpty()) {
                AlertUtil.showWarning("No Tiers Available", 
                    "This campaign does not have any subscription tiers configured yet.");
                closeDialog();
                return;
            }
            
            tierListView.setItems(FXCollections.observableArrayList(availableTiers));
            
            // Select first tier by default
            if (!availableTiers.isEmpty()) {
                tierListView.getSelectionModel().selectFirst();
            }
            
            logger.info("Loaded {} subscription tiers for campaign {}", availableTiers.size(), campaign.getId());
            
        } catch (BusinessException | ValidationException e) {
            logger.error("Error loading subscription tiers", e);
            AlertUtil.showError("Error", "Failed to load subscription tiers: " + e.getMessage());
            closeDialog();
        }
    }
    
    /**
     * Update tier details display.
     */
    private void updateTierDetails(SubscriptionTier tier) {
        if (tier != null) {
            monthlyAmountLabel.setText(String.format("$%.2f", tier.getMonthlyAmount()));
            
            StringBuilder benefits = new StringBuilder();
            if (tier.getDescription() != null && !tier.getDescription().isEmpty()) {
                benefits.append(tier.getDescription()).append("\n\n");
            }
            if (tier.getBenefits() != null && !tier.getBenefits().isEmpty()) {
                benefits.append("Benefits:\n").append(tier.getBenefits());
            }
            
            benefitsTextArea.setText(benefits.toString());
        }
    }
    
    /**
     * Handle subscribe button click.
     */
    @FXML
    private void handleSubscribe(ActionEvent event) {
        SubscriptionTier selectedTier = tierListView.getSelectionModel().getSelectedItem();
        
        if (selectedTier == null) {
            AlertUtil.showError("Validation Error", "Please select a subscription tier.");
            return;
        }
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertUtil.showError("Error", "You must be logged in to subscribe.");
            closeDialog();
            return;
        }
        
        try {
            subscriptionService.subscribeWithTier(
                campaign.getId(), 
                currentUser.getId(), 
                selectedTier.getId()
            );
            
            AlertUtil.showSuccess("Subscription Created", 
                "You have successfully subscribed to " + campaign.getTitle() + 
                " with the " + selectedTier.getTierName() + " tier!\n\n" +
                "Monthly amount: $" + String.format("%.2f", selectedTier.getMonthlyAmount()));
            
            closeDialog();
            
        } catch (BusinessException | ValidationException e) {
            logger.error("Error creating subscription", e);
            AlertUtil.showError("Subscription Failed", e.getMessage());
        }
    }
    
    /**
     * Handle cancel button click.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }
    
    /**
     * Close the dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) subscribeButton.getScene().getWindow();
        stage.close();
    }
}
