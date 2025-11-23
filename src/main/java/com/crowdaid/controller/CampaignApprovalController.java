package com.crowdaid.controller;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.CampaignStatus;
import com.crowdaid.model.user.Administrator;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
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

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for campaign approval (UC11: Approve Campaign).
 */
public class CampaignApprovalController {
    
    private static final Logger logger = LoggerFactory.getLogger(CampaignApprovalController.class);
    
    private final ViewLoader viewLoader;
    private final CampaignRepository campaignRepository;
    private Administrator admin;
    
    @FXML private TableView<Campaign> pendingCampaignsTable;
    @FXML private TableColumn<Campaign, String> titleColumn;
    @FXML private TableColumn<Campaign, String> campaignerColumn;
    @FXML private TableColumn<Campaign, String> categoryColumn;
    @FXML private TableColumn<Campaign, Double> goalColumn;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea rejectionReasonArea;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button backButton;
    
    private ObservableList<Campaign> pendingCampaigns;
    
    public CampaignApprovalController() {
        this.viewLoader = ViewLoader.getInstance();
        this.campaignRepository = new MySQLCampaignRepository();
        this.pendingCampaigns = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        admin = (Administrator) SessionManager.getInstance().getCurrentUser();
        
        // Setup table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        campaignerColumn.setCellValueFactory(new PropertyValueFactory<>("campaignerId"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        goalColumn.setCellValueFactory(new PropertyValueFactory<>("goalAmount"));
        
        pendingCampaignsTable.setItems(pendingCampaigns);
        
        // Show description when campaign is selected
        pendingCampaignsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                descriptionArea.setText(newSelection.getDescription());
            }
        });
        
        loadPendingCampaigns();
        
        logger.info("Campaign approval screen initialized");
    }
    
    /**
     * Load pending campaigns from database.
     */
    private void loadPendingCampaigns() {
        try {
            List<Campaign> campaigns = campaignRepository.findByStatus(CampaignStatus.PENDING_REVIEW);
            pendingCampaigns.clear();
            pendingCampaigns.addAll(campaigns);
            logger.info("Loaded {} pending campaigns for approval", campaigns.size());
        } catch (SQLException e) {
            logger.error("Error loading pending campaigns", e);
            AlertUtil.showError("Database Error", "Failed to load pending campaigns: " + e.getMessage());
        }
    }
    
    /**
     * Handle approve button click.
     */
    @FXML
    private void handleApprove(ActionEvent event) {
        Campaign selected = pendingCampaignsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a campaign to approve.");
            return;
        }
        
        boolean confirmed = AlertUtil.showConfirmation("Confirm Approval", 
            "Are you sure you want to approve campaign: " + selected.getTitle() + "?");
        
        if (confirmed) {
            try {
                campaignRepository.updateStatus(selected.getId(), CampaignStatus.ACTIVE);
                
                AlertUtil.showSuccess("Campaign Approved", 
                    "The campaign has been approved and is now ACTIVE for donations.");
                
                pendingCampaigns.remove(selected);
                descriptionArea.clear();
                
                logger.info("Campaign approved: id={}, title={}, admin={}", 
                           selected.getId(), selected.getTitle(), admin.getEmail());
                           
            } catch (SQLException e) {
                logger.error("Error approving campaign", e);
                AlertUtil.showError("Database Error", "Failed to approve campaign: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle reject button click.
     */
    @FXML
    private void handleReject(ActionEvent event) {
        Campaign selected = pendingCampaignsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a campaign to reject.");
            return;
        }
        
        String reason = rejectionReasonArea.getText();
        
        if (reason == null || reason.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Please provide a rejection reason.");
            return;
        }
        
        boolean confirmed = AlertUtil.showConfirmation("Confirm Rejection", 
            "Are you sure you want to reject campaign: " + selected.getTitle() + "?");
        
        if (confirmed) {
            try {
                campaignRepository.updateStatus(selected.getId(), CampaignStatus.REJECTED);
                
                AlertUtil.showSuccess("Campaign Rejected", 
                    "The campaign has been rejected. Reason: " + reason);
                
                pendingCampaigns.remove(selected);
                descriptionArea.clear();
                rejectionReasonArea.clear();
                
                logger.info("Campaign rejected: id={}, title={}, admin={}, reason={}", 
                           selected.getId(), selected.getTitle(), admin.getEmail(), reason);
                           
            } catch (SQLException e) {
                logger.error("Error rejecting campaign", e);
                AlertUtil.showError("Database Error", "Failed to reject campaign: " + e.getMessage());
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
