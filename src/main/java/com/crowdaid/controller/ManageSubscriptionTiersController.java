package com.crowdaid.controller;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.donation.SubscriptionTier;
import com.crowdaid.service.SubscriptionService;
import com.crowdaid.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller for managing subscription tiers for a campaign.
 * Allows campaigners to create, edit, and delete subscription tiers.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class ManageSubscriptionTiersController {
    
    private static final Logger logger = LoggerFactory.getLogger(ManageSubscriptionTiersController.class);
    
    private Campaign campaign;
    private SubscriptionService subscriptionService;
    private ObservableList<SubscriptionTier> tiers;
    
    @FXML private Label campaignTitleLabel;
    @FXML private TableView<SubscriptionTier> tiersTable;
    @FXML private TableColumn<SubscriptionTier, String> tierNameColumn;
    @FXML private TableColumn<SubscriptionTier, Double> amountColumn;
    @FXML private TableColumn<SubscriptionTier, String> descriptionColumn;
    
    @FXML private TextField tierNameField;
    @FXML private TextField monthlyAmountField;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea benefitsArea;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button closeButton;
    
    @FXML
    private void initialize() {
        subscriptionService = new SubscriptionService();
        tiers = FXCollections.observableArrayList();
        
        // Setup table columns
        tierNameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTierName())
        );
        
        amountColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getMonthlyAmount())
        );
        
        descriptionColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription())
        );
        
        tiersTable.setItems(tiers);
        
        // Add selection listener
        tiersTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    populateForm(newValue);
                }
            }
        );
        
        logger.info("Manage Subscription Tiers controller initialized");
    }
    
    /**
     * Sets the campaign for tier management and loads existing tiers.
     */
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
        campaignTitleLabel.setText("Manage Subscription Tiers for: " + campaign.getTitle());
        loadTiers();
    }
    
    /**
     * Loads subscription tiers for the campaign.
     */
    private void loadTiers() {
        try {
            List<SubscriptionTier> tierList = subscriptionService.getTiersByCampaign(campaign.getId());
            tiers.clear();
            tiers.addAll(tierList);
            logger.info("Loaded {} subscription tiers", tierList.size());
            
        } catch (BusinessException | ValidationException e) {
            logger.error("Error loading subscription tiers", e);
            AlertUtil.showError("Error", "Failed to load subscription tiers: " + e.getMessage());
        }
    }
    
    /**
     * Populates form fields with selected tier data.
     */
    private void populateForm(SubscriptionTier tier) {
        tierNameField.setText(tier.getTierName());
        monthlyAmountField.setText(String.valueOf(tier.getMonthlyAmount()));
        descriptionArea.setText(tier.getDescription());
        benefitsArea.setText(tier.getBenefits());
    }
    
    /**
     * Clears all form fields.
     */
    private void clearForm() {
        tierNameField.clear();
        monthlyAmountField.clear();
        descriptionArea.clear();
        benefitsArea.clear();
        tiersTable.getSelectionModel().clearSelection();
    }
    
    /**
     * Handles add tier button click.
     */
    @FXML
    private void handleAdd(ActionEvent event) {
        String tierName = tierNameField.getText();
        String amountText = monthlyAmountField.getText();
        String description = descriptionArea.getText();
        String benefits = benefitsArea.getText();
        
        if (tierName == null || tierName.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Please enter a tier name.");
            return;
        }
        
        if (amountText == null || amountText.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Please enter a monthly amount.");
            return;
        }
        
        double monthlyAmount;
        try {
            monthlyAmount = Double.parseDouble(amountText);
            if (monthlyAmount <= 0) {
                AlertUtil.showError("Validation Error", "Monthly amount must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Please enter a valid monthly amount.");
            return;
        }
        
        try {
            subscriptionService.createTier(
                campaign.getId(),
                tierName.trim(),
                monthlyAmount,
                description != null ? description.trim() : "",
                benefits != null ? benefits.trim() : ""
            );
            
            AlertUtil.showSuccess("Success", "Subscription tier created successfully!");
            clearForm();
            loadTiers();
            
        } catch (BusinessException | ValidationException e) {
            logger.error("Error creating subscription tier", e);
            AlertUtil.showError("Error", "Failed to create subscription tier: " + e.getMessage());
        }
    }
    
    /**
     * Handles update tier button click.
     */
    @FXML
    private void handleUpdate(ActionEvent event) {
        SubscriptionTier selectedTier = tiersTable.getSelectionModel().getSelectedItem();
        
        if (selectedTier == null) {
            AlertUtil.showWarning("No Selection", "Please select a tier to update.");
            return;
        }
        
        String tierName = tierNameField.getText();
        String amountText = monthlyAmountField.getText();
        String description = descriptionArea.getText();
        String benefits = benefitsArea.getText();
        
        if (tierName == null || tierName.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Please enter a tier name.");
            return;
        }
        
        if (amountText == null || amountText.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Please enter a monthly amount.");
            return;
        }
        
        double monthlyAmount;
        try {
            monthlyAmount = Double.parseDouble(amountText);
            if (monthlyAmount <= 0) {
                AlertUtil.showError("Validation Error", "Monthly amount must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Please enter a valid monthly amount.");
            return;
        }
        
        try {
            subscriptionService.updateTier(
                selectedTier.getId(),
                tierName.trim(),
                monthlyAmount,
                description != null ? description.trim() : "",
                benefits != null ? benefits.trim() : ""
            );
            
            AlertUtil.showSuccess("Success", "Subscription tier updated successfully!");
            clearForm();
            loadTiers();
            
        } catch (BusinessException | ValidationException e) {
            logger.error("Error updating subscription tier", e);
            AlertUtil.showError("Error", "Failed to update subscription tier: " + e.getMessage());
        }
    }
    
    /**
     * Handles delete tier button click.
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        SubscriptionTier selectedTier = tiersTable.getSelectionModel().getSelectedItem();
        
        if (selectedTier == null) {
            AlertUtil.showWarning("No Selection", "Please select a tier to delete.");
            return;
        }
        
        boolean confirm = AlertUtil.showConfirmation("Delete Tier",
            "Are you sure you want to delete this subscription tier?\n" +
            "This action cannot be undone if there are no active subscriptions.");
        
        if (confirm) {
            try {
                subscriptionService.deleteTier(selectedTier.getId());
                AlertUtil.showSuccess("Success", "Subscription tier deleted successfully!");
                clearForm();
                loadTiers();
                
            } catch (BusinessException | ValidationException e) {
                logger.error("Error deleting subscription tier", e);
                AlertUtil.showError("Error", "Failed to delete subscription tier: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles clear button click.
     */
    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }
    
    /**
     * Handles close button click.
     */
    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
