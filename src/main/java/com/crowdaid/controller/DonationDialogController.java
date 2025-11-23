package com.crowdaid.controller;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.donation.Donation;
import com.crowdaid.model.user.Donor;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.interfaces.DonationRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.repository.mysql.MySQLDonationRepository;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Controller for donation dialog (UC7: Make One-Time Donation).
 */
public class DonationDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(DonationDialogController.class);
    
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private Campaign campaign;
    private Donor donor;
    
    @FXML private Label campaignLabel;
    @FXML private TextField amountField;
    @FXML private CheckBox anonymousCheckBox;
    @FXML private TextArea messageArea;
    @FXML private Button donateButton;
    @FXML private Button cancelButton;
    
    public DonationDialogController() {
        this.donationRepository = new MySQLDonationRepository();
        this.campaignRepository = new MySQLCampaignRepository();
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
            
            // Create donation
            Donation donation = new Donation();
            donation.setCampaignId(campaign.getId());
            donation.setDonorId(donor.getId());
            donation.setAmount(amount);
            donation.setAnonymous(anonymous);
            donation.setMessage(message);
            donation.setTransactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            
            // Save donation
            Donation savedDonation = donationRepository.save(donation);
            
            // Update campaign collected amount
            double newCollectedAmount = campaign.getCollectedAmount() + amount;
            campaignRepository.updateCollectedAmount(campaign.getId(), newCollectedAmount);
            
            logger.info("Donation successful: amount={}, campaign={}, donor={}, anonymous={}", 
                       amount, campaign.getTitle(), donor.getEmail(), anonymous);
            
            AlertUtil.showSuccess("Donation Successful", 
                String.format("Thank you for donating $%.2f to %s!\\nTransaction: %s", 
                             amount, campaign.getTitle(), savedDonation.getTransactionReference()));
            
            closeDialog();
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Amount", "Please enter a valid donation amount (numbers only).");
            logger.warn("Invalid donation amount entered: {}", amountText);
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to process donation: " + e.getMessage());
            logger.error("Error processing donation", e);
        } catch (Exception e) {
            AlertUtil.showError("Error", "An unexpected error occurred: " + e.getMessage());
            logger.error("Unexpected error during donation", e);
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
