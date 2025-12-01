package com.crowdaid.controller;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.model.reward.Reward;
import com.crowdaid.model.user.Donor;
import com.crowdaid.model.user.User;
import com.crowdaid.service.CreditService;
import com.crowdaid.service.RewardService;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Redeem Reward dialog.
 * Handles reward redemption process (UC10).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class RedeemRewardController {
    
    private static final Logger logger = LoggerFactory.getLogger(RedeemRewardController.class);
    
    private final RewardService rewardService;
    private final CreditService creditService;
    private Donor currentDonor;
    private Reward currentReward;
    
    @FXML private Label rewardNameLabel;
    @FXML private Label rewardCategoryLabel;
    @FXML private TextArea rewardDescriptionArea;
    @FXML private Label creditCostLabel;
    @FXML private Label currentBalanceLabel;
    @FXML private Label stockLabel;
    @FXML private Button redeemButton;
    @FXML private Button cancelButton;
    
    public RedeemRewardController() {
        this.rewardService = new RewardService();
        this.creditService = new CreditService();
    }
    
    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        
        if (user == null || !(user instanceof Donor)) {
            AlertUtil.showError("Access Denied", "You must be logged in as a donor.");
            closeDialog();
            return;
        }
        
        currentDonor = (Donor) user;
        updateCreditBalance();
    }
    
    /**
     * Sets the reward to display and validates.
     * 
     * @param reward the reward to redeem
     */
    public void setReward(Reward reward) {
        this.currentReward = reward;
        displayRewardDetails();
        validateRedemption();
    }
    
    /**
     * Displays reward details in the UI.
     */
    private void displayRewardDetails() {
        if (currentReward == null) return;
        
        rewardNameLabel.setText(currentReward.getName());
        rewardCategoryLabel.setText(currentReward.getCategory().toString());
        rewardDescriptionArea.setText(currentReward.getDescription());
        creditCostLabel.setText(String.valueOf(currentReward.getCreditCost()));
        stockLabel.setText(String.valueOf(currentReward.getStockQuantity()));
    }
    
    /**
     * Updates the credit balance display.
     */
    private void updateCreditBalance() {
        try {
            int balance = creditService.getCreditBalance(currentDonor.getId());
            currentBalanceLabel.setText(String.valueOf(balance));
        } catch (BusinessException e) {
            logger.error("Error loading credit balance", e);
            currentBalanceLabel.setText("N/A");
        }
    }
    
    /**
     * Validates if redemption is possible and enables/disables button.
     */
    private void validateRedemption() {
        if (currentReward == null) {
            redeemButton.setDisable(true);
            return;
        }
        
        try {
            int balance = creditService.getCreditBalance(currentDonor.getId());
            boolean hasSufficientCredits = balance >= currentReward.getCreditCost();
            boolean hasStock = currentReward.getStockQuantity() > 0;
            boolean isAvailable = currentReward.isAvailable();
            
            redeemButton.setDisable(!hasSufficientCredits || !hasStock || !isAvailable);
            
            if (!hasSufficientCredits) {
                AlertUtil.showWarning("Insufficient Credits", 
                        "You need " + currentReward.getCreditCost() + " credits but only have " + balance);
            } else if (!hasStock) {
                AlertUtil.showWarning("Out of Stock", "This reward is currently out of stock.");
            }
        } catch (BusinessException e) {
            logger.error("Error validating redemption", e);
            redeemButton.setDisable(true);
        }
    }
    
    /**
     * Handles redeem button click (UC10: Redeem Credits).
     */
    @FXML
    private void handleRedeem(ActionEvent event) {
        if (currentReward == null) return;
        
        boolean confirm = AlertUtil.showConfirmation("Confirm Redemption",
                "Redeem " + currentReward.getName() + " for " + currentReward.getCreditCost() + " credits?");
        
        if (!confirm) return;
        
        try {
            rewardService.redeemReward(currentDonor.getId(), currentReward.getId());
            
            AlertUtil.showInfo("Success", "Reward redeemed successfully! " +
                    "You will receive further instructions via email.");
            
            logger.info("Reward redeemed: donor={}, reward={}", currentDonor.getId(), currentReward.getId());
            
            closeDialog();
        } catch (com.crowdaid.exception.ValidationException e) {
            logger.error("Validation error redeeming reward", e);
            AlertUtil.showError("Validation Error", e.getMessage());
        } catch (BusinessException e) {
            logger.error("Error redeeming reward", e);
            AlertUtil.showError("Redemption Failed", e.getMessage());
        }
    }
    
    /**
     * Handles cancel button click.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
