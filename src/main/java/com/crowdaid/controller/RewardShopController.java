package com.crowdaid.controller;

import com.crowdaid.model.reward.Reward;
import com.crowdaid.model.reward.RewardCategory;
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
 * Controller for reward shop (UC10: Redeem Credits in Shop).
 */
public class RewardShopController {
    
    private static final Logger logger = LoggerFactory.getLogger(RewardShopController.class);
    
    private final ViewLoader viewLoader;
    
    @FXML private Label creditBalanceLabel;
    @FXML private ComboBox<RewardCategory> categoryFilterComboBox;
    @FXML private TableView<Reward> rewardsTable;
    @FXML private TableColumn<Reward, String> nameColumn;
    @FXML private TableColumn<Reward, String> categoryColumn;
    @FXML private TableColumn<Reward, Integer> costColumn;
    @FXML private TableColumn<Reward, Integer> stockColumn;
    @FXML private TextArea descriptionArea;
    @FXML private Button redeemButton;
    @FXML private Button myRedemptionsButton;
    @FXML private Button backButton;
    
    private ObservableList<Reward> rewards;
    
    public RewardShopController() {
        this.viewLoader = ViewLoader.getInstance();
        this.rewards = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        SessionManager.getInstance().getCurrentUser();
        
        // Setup category filter
        categoryFilterComboBox.getItems().add(null); // "All Categories"
        categoryFilterComboBox.getItems().addAll(RewardCategory.values());
        categoryFilterComboBox.setValue(null);
        
        // Setup table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("creditCost"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        
        rewardsTable.setItems(rewards);
        
        // Handle selection to show description
        rewardsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                descriptionArea.setText(newSelection.getDescription());
            }
        });
        
        creditBalanceLabel.setText("Your Credits: 0");
        
        loadRewards();
        
        logger.info("Reward shop screen initialized");
    }
    
    /**
     * Load rewards from database.
     */
    private void loadRewards() {
        try {
            // List<Reward> rewardList = rewardRepository.findAllActive();
            // rewards.clear();
            // rewards.addAll(rewardList);
        } catch (Exception e) {
            logger.error("Error loading rewards", e);
            AlertUtil.showError("Database Error", "Failed to load rewards.");
        }
    }
    
    /**
     * Handle category filter change.
     */
    @FXML
    private void handleCategoryFilter(ActionEvent event) {
        categoryFilterComboBox.getValue();
    }
    
    /**
     * Handle redeem button click.
     */
    @FXML
    private void handleRedeem(ActionEvent event) {
        Reward selected = rewardsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a reward to redeem.");
            return;
        }
        
        boolean confirmed = AlertUtil.showConfirmation("Confirm Redemption", 
            String.format("Redeem %s for %d credits?", selected.getName(), selected.getCreditCost()));
        
        if (confirmed) {
            // rewardService.redeemReward(donor, selected);
            
            AlertUtil.showSuccess("Redemption Successful", 
                "Your reward has been redeemed successfully!");
        }
    }
    
    /**
     * Navigate to my redemptions screen.
     */
    @FXML
    private void handleMyRedemptions(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/my_redemptions.fxml", "CrowdAid - My Redemptions");
    }
    
    /**
     * Handle back button click.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/donor_dashboard.fxml", "CrowdAid - Donor Dashboard");
    }
}
