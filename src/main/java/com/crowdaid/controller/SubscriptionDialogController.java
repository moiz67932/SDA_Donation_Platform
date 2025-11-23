package com.crowdaid.controller;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for subscription dialog (UC8: Subscribe to Campaign).
 */
public class SubscriptionDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionDialogController.class);
    
    private Campaign campaign;
    
    @FXML private Label campaignLabel;
    @FXML private ComboBox<String> tierComboBox;
    @FXML private Label tierAmountLabel;
    @FXML private CheckBox anonymousCheckBox;
    @FXML private Button subscribeButton;
    @FXML private Button cancelButton;
    
    @FXML
    private void initialize() {
        SessionManager.getInstance().getCurrentUser();
        
        // Setup tier options (example tiers)
        tierComboBox.getItems().addAll("Bronze ($10/month)", "Silver ($25/month)", "Gold ($50/month)");
        
        tierComboBox.setOnAction(e -> updateTierAmount());
        
        logger.info("Subscription dialog initialized");
    }
    
    /**
     * Set the campaign for subscription.
     */
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
        campaignLabel.setText("Subscribe to: " + campaign.getTitle());
    }
    
    /**
     * Update tier amount label based on selection.
     */
    private void updateTierAmount() {
        String tier = tierComboBox.getValue();
        if (tier != null) {
            if (tier.contains("Bronze")) {
                tierAmountLabel.setText("$10.00 per month");
            } else if (tier.contains("Silver")) {
                tierAmountLabel.setText("$25.00 per month");
            } else if (tier.contains("Gold")) {
                tierAmountLabel.setText("$50.00 per month");
            }
        }
    }
    
    /**
     * Handle subscribe button click.
     */
    @FXML
    private void handleSubscribe(ActionEvent event) {
        String tier = tierComboBox.getValue();
        
        if (tier == null) {
            AlertUtil.showError("Validation Error", "Please select a subscription tier.");
            return;
        }
        
        anonymousCheckBox.isSelected();
        
        // TODO: Call SubscriptionService.createSubscription()
        // subscriptionService.createSubscription(donor, campaign, tier, anonymous);
        
        AlertUtil.showSuccess("Subscription Created", 
            "You have successfully subscribed to " + campaign.getTitle() + "!");
        
        closeDialog();
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
