package com.crowdaid.controller;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.model.donation.Subscription;
import com.crowdaid.model.donation.SubscriptionStatus;
import com.crowdaid.model.user.Donor;
import com.crowdaid.model.user.User;
import com.crowdaid.service.SubscriptionService;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller for the My Subscriptions screen.
 * Displays and manages donor's active subscriptions.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySubscriptionsController {
    
    private static final Logger logger = LoggerFactory.getLogger(MySubscriptionsController.class);
    
    private final SubscriptionService subscriptionService;
    private final ViewLoader viewLoader;
    private Donor currentDonor;
    
    @FXML private TableView<Subscription> subscriptionsTable;
    @FXML private TableColumn<Subscription, String> campaignColumn;
    @FXML private TableColumn<Subscription, String> tierColumn;
    @FXML private TableColumn<Subscription, Double> amountColumn;
    @FXML private TableColumn<Subscription, String> startDateColumn;
    @FXML private TableColumn<Subscription, String> nextPaymentColumn;
    @FXML private TableColumn<Subscription, String> statusColumn;
    
    @FXML private Button pauseButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    
    public MySubscriptionsController() {
        this.subscriptionService = new SubscriptionService();
        this.viewLoader = ViewLoader.getInstance();
    }
    
    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        
        if (user == null || !(user instanceof Donor)) {
            AlertUtil.showError("Access Denied", "You must be logged in as a donor.");
            viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
            return;
        }
        
        currentDonor = (Donor) user;
        
        setupTableColumns();
        loadSubscriptions();
        
        logger.info("My Subscriptions loaded for donor: {}", currentDonor.getEmail());
    }
    
    /**
     * Sets up table columns with property value factories.
     */
    private void setupTableColumns() {
        // Note: Campaign titles and tier names will be loaded separately
        campaignColumn.setCellValueFactory(cellData -> {
            Long campaignId = cellData.getValue().getCampaignId();
            return new javafx.beans.property.SimpleStringProperty(
                campaignId != null ? "Campaign #" + campaignId : "Unknown"
            );
        });
        
        tierColumn.setCellValueFactory(cellData -> {
            String tierName = cellData.getValue().getTierName();
            return new javafx.beans.property.SimpleStringProperty(
                tierName != null ? tierName : "N/A"
            );
        });
        
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("monthlyAmount"));
        
        startDateColumn.setCellValueFactory(cellData -> {
            java.time.LocalDate startDate = cellData.getValue().getStartDate();
            return new javafx.beans.property.SimpleStringProperty(
                startDate != null ? startDate.toString() : "N/A"
            );
        });
        
        nextPaymentColumn.setCellValueFactory(cellData -> {
            java.time.LocalDate nextBilling = cellData.getValue().getNextBillingDate();
            return new javafx.beans.property.SimpleStringProperty(
                nextBilling != null ? nextBilling.toString() : "N/A"
            );
        });
        
        statusColumn.setCellValueFactory(cellData -> {
            SubscriptionStatus status = cellData.getValue().getStatus();
            return new javafx.beans.property.SimpleStringProperty(
                status != null ? status.name() : "UNKNOWN"
            );
        });
    }
    
    /**
     * Loads subscriptions from the service.
     */
    private void loadSubscriptions() {
        try {
            List<Subscription> subscriptions = subscriptionService.getSubscriptionsByDonor(currentDonor.getId());
            ObservableList<Subscription> subscriptionList = FXCollections.observableArrayList(subscriptions);
            subscriptionsTable.setItems(subscriptionList);
            
            logger.info("Loaded {} subscriptions for donor {}", subscriptions.size(), currentDonor.getId());
        } catch (Exception e) {
            logger.error("Error loading subscriptions for donor {}", currentDonor.getId(), e);
            AlertUtil.showError("Error", "Failed to retrieve subscriptions: " + e.getMessage());
        }
    }
    
    /**
     * Handles pause subscription button click.
     */
    @FXML
    private void handlePause(ActionEvent event) {
        Subscription selected = subscriptionsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a subscription to pause.");
            return;
        }
        
        try {
            subscriptionService.updateSubscriptionStatus(selected.getId(), SubscriptionStatus.PAUSED);
            AlertUtil.showInfo("Success", "Subscription paused successfully.");
            loadSubscriptions();
        } catch (BusinessException e) {
            logger.error("Error pausing subscription", e);
            AlertUtil.showError("Error", "Failed to pause subscription: " + e.getMessage());
        }
    }
    
    /**
     * Handles cancel subscription button click.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        Subscription selected = subscriptionsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a subscription to cancel.");
            return;
        }
        
        boolean confirm = AlertUtil.showConfirmation("Cancel Subscription", 
                "Are you sure you want to cancel this subscription?");
        
        if (confirm) {
            try {
                subscriptionService.updateSubscriptionStatus(selected.getId(), SubscriptionStatus.CANCELLED);
                AlertUtil.showInfo("Success", "Subscription cancelled successfully.");
                loadSubscriptions();
            } catch (BusinessException e) {
                logger.error("Error cancelling subscription", e);
                AlertUtil.showError("Error", "Failed to cancel subscription: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles back to dashboard button click.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
                "/fxml/donor_dashboard.fxml", "CrowdAid - Donor Dashboard");
    }
}
