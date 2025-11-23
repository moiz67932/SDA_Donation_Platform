package com.crowdaid.controller;

import com.crowdaid.model.reward.Reward;
import com.crowdaid.model.reward.RewardCategory;
import com.crowdaid.model.reward.RewardStatus;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for reward management (UC12: Edit Reward Shop).
 */
public class RewardManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(RewardManagementController.class);
    
    private final ViewLoader viewLoader;
    
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<RewardCategory> categoryComboBox;
    @FXML private TextField creditCostField;
    @FXML private TextField stockField;
    @FXML private ComboBox<RewardStatus> statusComboBox;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private TableView<Reward> rewardsTable;
    @FXML private TableColumn<Reward, String> nameColumn;
    @FXML private TableColumn<Reward, String> categoryColumn;
    @FXML private TableColumn<Reward, Integer> costColumn;
    @FXML private TableColumn<Reward, Integer> stockColumn;
    @FXML private TableColumn<Reward, String> statusColumn;
    @FXML private Button backButton;
    
    private ObservableList<Reward> rewards;
    private Reward selectedReward;
    
    public RewardManagementController() {
        this.viewLoader = ViewLoader.getInstance();
        this.rewards = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        SessionManager.getInstance().getCurrentUser();
        
        // Setup combo boxes
        categoryComboBox.getItems().addAll(RewardCategory.values());
        statusComboBox.getItems().addAll(RewardStatus.values());
        statusComboBox.setValue(RewardStatus.AVAILABLE);
        
        // Setup table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("creditCost"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        rewardsTable.setItems(rewards);
        
        // Handle selection to populate form
        rewardsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedReward = newSelection;
                populateForm(newSelection);
            }
        });
        
        // TODO: Load all rewards
        loadRewards();
        
        logger.info("Reward management screen initialized");
    }
    
    /**
     * Load rewards from database.
     */
    private void loadRewards() {
        try {
            // TODO: Call RewardRepository.findAll()
            // List<Reward> rewardList = rewardRepository.findAll();
            // rewards.clear();
            // rewards.addAll(rewardList);
        } catch (Exception e) {
            logger.error("Error loading rewards", e);
            AlertUtil.showError("Database Error", "Failed to load rewards.");
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
        statusComboBox.setValue(reward.getStatus());
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
        statusComboBox.setValue(RewardStatus.AVAILABLE);
        selectedReward = null;
        rewardsTable.getSelectionModel().clearSelection();
    }
    
    /**
     * Handle add button click.
     */
    @FXML
    private void handleAdd(ActionEvent event) {
        String name = nameField.getText();
        descriptionArea.getText();
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
            Integer.parseInt(creditCostField.getText());
            Integer.parseInt(stockField.getText());
            statusComboBox.getValue();
            
            // TODO: Call RewardService.createReward()
            // Reward reward = rewardService.createReward(name, description, category, creditCost, stock, status);
            
            AlertUtil.showSuccess("Reward Added", "The reward has been added successfully.");
            
            clearForm();
            loadRewards();
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Please enter valid numbers for cost and stock.");
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
            selectedReward.setCreditCost(Integer.parseInt(creditCostField.getText()));
            selectedReward.setStock(Integer.parseInt(stockField.getText()));
            selectedReward.setStatus(statusComboBox.getValue());
            
            // TODO: Call RewardService.updateReward()
            // rewardService.updateReward(selectedReward);
            
            AlertUtil.showSuccess("Reward Updated", "The reward has been updated successfully.");
            
            clearForm();
            loadRewards();
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Please enter valid numbers for cost and stock.");
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
            // TODO: Call RewardService.deleteReward()
            // rewardService.deleteReward(selectedReward.getId());
            
            AlertUtil.showSuccess("Reward Deleted", "The reward has been deleted.");
            
            clearForm();
            loadRewards();
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
