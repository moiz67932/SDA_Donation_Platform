package com.crowdaid.controller;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.donation.Donation;
import com.crowdaid.model.user.Donor;
import com.crowdaid.service.DonationService;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for donation dialog (UC7: Make One-Time Donation).
 * Updated to use DonationService which handles credit awarding for COMMUNITY category donations.
 */
public class DonationDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(DonationDialogController.class);
    
    private final DonationService donationService;
    private Campaign campaign;
    private Donor donor;
    
    @FXML private Label campaignLabel;
    @FXML private TextField amountField;
    @FXML private CheckBox anonymousCheckBox;
    @FXML private TextArea messageArea;
    @FXML private Button donateButton;
    @FXML private Button cancelButton;
    
    public DonationDialogController() {
        this.donationService = new DonationService();
    }
    
    @FXML
    private void initialize() {
        donor = (Donor) SessionManager.getInstance().getCurrentUser();
        campaign = (Campaign) SessionManager.getInstance().getAttribute("selectedCampaign");
        
        if (campaign != null) {
            campaignLabel.setText("Donate to: " + campaign.getTitle());
        }
        
        logger.info("Donation dialog initialized for campaign: {}", 
                   campaign != null ? campaign.getTitle() : "none");
    }
    
    /**
     * Handle donate button click.
     */
    @FXML
    private void handleDonate(ActionEvent event) {
        if (campaign == null) {
            AlertUtil.showError("Error", "No campaign selected.");
            closeDialog();
            return;
        }
        
        String amountText = amountField.getText();
        
        // Validation
        if (amountText == null || amountText.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Please enter a donation amount.");
            return;
        }
        
        try {
            double amount = Double.parseDouble(amountText);
            
            if (amount <= 0) {
                AlertUtil.showError("Validation Error", "Donation amount must be greater than zero.");
                return;
            }
            
            if (amount > 1000000) {
                AlertUtil.showError("Validation Error", "Donation amount cannot exceed $1,000,000.");
                return;
            }
            
            boolean anonymous = anonymousCheckBox.isSelected();
            String message = messageArea.getText();
            
            // Process donation through DonationService (handles credit awarding for COMMUNITY campaigns)
            Donation savedDonation = donationService.makeDonation(
                campaign.getId(), 
                donor.getId(), 
                amount, 
                anonymous, 
                message
            );
            
            logger.info("Donation successful: amount={}, campaign={}, donor={}, anonymous={}", 
                       amount, campaign.getTitle(), donor.getEmail(), anonymous);
            
            // Show success message with transaction reference
            String successMessage = String.format(
                "Thank you for donating $%.2f to %s!\nTransaction: %s", 
                amount, campaign.getTitle(), savedDonation.getTransactionReference()
            );
            
            // Add credit info for COMMUNITY category donations
            if (campaign.getCategory() == com.crowdaid.model.campaign.CampaignCategory.COMMUNITY) {
                int creditsEarned = (int) Math.floor(amount / 100.0);
                if (creditsEarned > 0) {
                    successMessage += String.format("\n\nYou earned %d credit%s for this donation!", 
                        creditsEarned, creditsEarned > 1 ? "s" : "");
                }
            }
            
            AlertUtil.showSuccess("Donation Successful", successMessage);
            
            // Signal that credits should be refreshed on dashboard
            SessionManager.getInstance().setAttribute("refreshCredits", true);
            
            closeDialog();
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Amount", "Please enter a valid donation amount (numbers only).");
            logger.warn("Invalid donation amount entered: {}", amountText);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to process donation: " + e.getMessage());
            logger.error("Error processing donation", e);
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
        Stage stage = (Stage) donateButton.getScene().getWindow();
        stage.close();
    }
}
