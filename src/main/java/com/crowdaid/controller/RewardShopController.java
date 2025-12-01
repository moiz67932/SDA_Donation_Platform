package com.crowdaid.controller;

import com.crowdaid.model.reward.Reward;
import com.crowdaid.model.reward.RewardCategory;
import com.crowdaid.model.user.Donor;
import com.crowdaid.model.user.User;
import com.crowdaid.service.CreditService;
import com.crowdaid.service.RewardService;
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
    private final RewardService rewardService;
    private final CreditService creditService;
    private Donor currentDonor;
    
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
        this.rewardService = new RewardService();
        this.creditService = new CreditService();
        this.rewards = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        
        if (user == null || !(user instanceof Donor)) {
            AlertUtil.showError("Access Denied", "You must be logged in as a donor to access this page.");
            viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
            return;
        }
        
        currentDonor = (Donor) user;
        
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
        
        loadCreditBalance();
        loadRewards();
        
        logger.info("Reward shop screen initialized for donor: {}", currentDonor.getEmail());
    }
    
    /**
     * Load and display the donor's credit balance.
     */
    private void loadCreditBalance() {
        try {
            int credits = creditService.getCreditBalance(currentDonor.getId());
            creditBalanceLabel.setText("Your Credits: " + credits);
            creditBalanceLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;"); // Green color
            logger.debug("Credit balance loaded: {}", credits);
        } catch (Exception e) {
            logger.error("Error loading credit balance", e);
            creditBalanceLabel.setText("Your Credits: 0");
        }
    }
    
    /**
     * Public method to refresh credit balance (can be called from other controllers).
     */
    public void refreshCreditBalance() {
        loadCreditBalance();
    }
    
    /**
     * Load rewards from database.
     */
    private void loadRewards() {
        try {
            java.util.List<Reward> rewardList = rewardService.browseAvailableRewards();
            rewards.clear();
            rewards.addAll(rewardList);
            logger.info("Loaded {} available rewards", rewardList.size());
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
        RewardCategory selectedCategory = categoryFilterComboBox.getValue();
        
        try {
            if (selectedCategory == null) {
                // Load all rewards
                loadRewards();
            } else {
                // Filter by category
                java.util.List<Reward> rewardList = rewardService.getRewardsByCategory(selectedCategory);
                rewards.clear();
                rewards.addAll(rewardList);
                logger.info("Filtered {} rewards for category {}", rewardList.size(), selectedCategory);
            }
        } catch (Exception e) {
            logger.error("Error filtering rewards", e);
            AlertUtil.showError("Error", "Failed to filter rewards.");
        }
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
        
        try {
            // Check if donor has enough credits
            int currentCredits = creditService.getCreditBalance(currentDonor.getId());
            int requiredCredits = (int) selected.getCreditCost();
            
            if (currentCredits < requiredCredits) {
                AlertUtil.showWarning("Insufficient Credits", 
                    String.format("You need %d credits but only have %d credits.", 
                                 requiredCredits, currentCredits));
                return;
            }
            
            // Confirm redemption
            boolean confirmed = AlertUtil.showConfirmation("Confirm Redemption", 
                String.format("Redeem %s for %d credits?", selected.getName(), requiredCredits));
            
            if (confirmed) {
                // Redeem the reward
                rewardService.redeemReward(selected.getId(), currentDonor.getId(), "To be provided");
                
                // Refresh credit balance and rewards list
                loadCreditBalance();
                loadRewards();
                
                AlertUtil.showSuccess("Redemption Successful", 
                    String.format("You have successfully redeemed %s! Your remaining credits: %d", 
                                 selected.getName(), currentCredits - requiredCredits));
                
                logger.info("Donor {} successfully redeemed reward {} for {} credits", 
                           currentDonor.getId(), selected.getName(), requiredCredits);
            }
        } catch (Exception e) {
            logger.error("Error redeeming reward", e);
            AlertUtil.showError("Redemption Failed", "Failed to redeem reward: " + e.getMessage());
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
