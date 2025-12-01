package com.crowdaid.controller;

import com.crowdaid.model.reward.Reward;
import com.crowdaid.model.reward.RewardCategory;
import com.crowdaid.model.reward.RewardStatus;
import com.crowdaid.service.RewardService;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for reward management (UC12: Edit Reward Shop).
 */
public class RewardManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(RewardManagementController.class);
    
    private final ViewLoader viewLoader;
    private final RewardService rewardService;
    
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<RewardCategory> categoryComboBox;
    @FXML private TextField creditCostField;
    @FXML private TextField stockField;
    @FXML private CheckBox availableCheckBox;
    @FXML private ListView<Reward> rewardListView;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button newRewardButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;
    
    private ObservableList<Reward> rewards;
    private Reward selectedReward;
    
    public RewardManagementController() {
        this.viewLoader = ViewLoader.getInstance();
        this.rewardService = new RewardService();
        this.rewards = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        SessionManager.getInstance().getCurrentUser();
        
        // Setup combo boxes
        categoryComboBox.getItems().addAll(RewardCategory.values());
        
        // Configure ListView with custom cell factory to display reward info
        rewardListView.setCellFactory(param -> new ListCell<Reward>() {
            @Override
            protected void updateItem(Reward reward, boolean empty) {
                super.updateItem(reward, empty);
                if (empty || reward == null) {
                    setText(null);
                } else {
                    setText(reward.getName() + " (" + reward.getCreditCost() + " credits)");
                }
            }
        });
        
        rewardListView.setItems(rewards);
        
        // Handle selection to populate form
        rewardListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedReward = newSelection;
                populateForm(newSelection);
            }
        });
        
        loadRewards();
        
        logger.info("Reward management screen initialized");
    }
    
    /**
     * Load rewards from database.
     */
    private void loadRewards() {
        try {
            rewards.clear();
            rewards.addAll(rewardService.getAllRewards());
            logger.info("Loaded {} rewards", rewards.size());
        } catch (Exception e) {
            logger.error("Error loading rewards", e);
            AlertUtil.showError("Database Error", "Failed to load rewards: " + e.getMessage());
        }
    }
    
    /**
     * Populate form with reward data.
     */
    private void populateForm(Reward reward) {
        nameField.setText(reward.getName());
        descriptionArea.setText(reward.getDescription());
        categoryComboBox.setValue(reward.getCategory());
        creditCostField.setText(String.valueOf(reward.getCreditCost()));
        stockField.setText(String.valueOf(reward.getStock()));
        availableCheckBox.setSelected(reward.getStatus() == RewardStatus.AVAILABLE);
    }
    
    /**
     * Clear form fields.
     */
    private void clearForm() {
        nameField.clear();
        descriptionArea.clear();
        categoryComboBox.setValue(null);
        creditCostField.clear();
        stockField.clear();
        availableCheckBox.setSelected(true);
        selectedReward = null;
        rewardListView.getSelectionModel().clearSelection();
    }
    
    /**
     * Handle clear button click.
     */
    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }
    
    /**
     * Handle save button click (creates new or updates existing).
     */
    @FXML
    private void handleSave(ActionEvent event) {
        if (selectedReward == null) {
            handleAdd(event);
        } else {
            handleUpdate(event);
        }
    }
    
    /**
     * Handle add button click.
     */
    @FXML
    private void handleAdd(ActionEvent event) {
        String name = nameField.getText();
        String description = descriptionArea.getText();
        RewardCategory category = categoryComboBox.getValue();
        
        if (name == null || name.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Reward name is required.");
            return;
        }
        
        if (category == null) {
            AlertUtil.showError("Validation Error", "Category is required.");
            return;
        }
        
        try {
            double creditCost = Double.parseDouble(creditCostField.getText());
            int stock = Integer.parseInt(stockField.getText());
            
            // Validate inputs are parsed successfully before proceeding
            if (creditCost < 0 || stock < 0) {
                AlertUtil.showError("Validation Error", "Credit cost and stock must be positive numbers.");
                return;
            }
            
            // Create and save reward
            rewardService.createReward(name, description, category, creditCost, stock, null);
            
            AlertUtil.showSuccess("Reward Added", "The reward has been added successfully.");
            
            clearForm();
            loadRewards();
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Please enter valid numbers for cost and stock.");
        } catch (Exception e) {
            logger.error("Error creating reward", e);
            AlertUtil.showError("Error", "Failed to create reward: " + e.getMessage());
        }
    }
    
    /**
     * Handle update button click.
     */
    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedReward == null) {
            AlertUtil.showWarning("No Selection", "Please select a reward to update.");
            return;
        }
        
        try {
            selectedReward.setName(nameField.getText());
            selectedReward.setDescription(descriptionArea.getText());
            selectedReward.setCategory(categoryComboBox.getValue());
            selectedReward.setCreditCost(Double.parseDouble(creditCostField.getText()));
            selectedReward.setStock(Integer.parseInt(stockField.getText()));
            selectedReward.setStatus(availableCheckBox.isSelected() ? RewardStatus.AVAILABLE : RewardStatus.DISABLED);
            
            rewardService.updateReward(selectedReward);
            
            AlertUtil.showSuccess("Reward Updated", "The reward has been updated successfully.");
            
            clearForm();
            loadRewards();
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Please enter valid numbers for cost and stock.");
        } catch (Exception e) {
            logger.error("Error updating reward", e);
            AlertUtil.showError("Error", "Failed to update reward: " + e.getMessage());
        }
    }
    
    /**
     * Handle delete button click.
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedReward == null) {
            AlertUtil.showWarning("No Selection", "Please select a reward to delete.");
            return;
        }
        
        boolean confirmed = AlertUtil.showConfirmation("Confirm Delete", 
            "Are you sure you want to delete this reward?");
        
        if (confirmed) {
            try {
                rewardService.deleteReward(selectedReward.getId());
                
                AlertUtil.showSuccess("Reward Deleted", "The reward has been deleted.");
                
                clearForm();
                loadRewards();
            } catch (Exception e) {
                logger.error("Error deleting reward", e);
                AlertUtil.showError("Error", "Failed to delete reward: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle back button click.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/admin_dashboard.fxml", "CrowdAid - Admin Dashboard");
    }
}
