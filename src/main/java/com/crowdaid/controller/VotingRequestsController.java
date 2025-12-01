package com.crowdaid.controller;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.model.campaign.Milestone;
import com.crowdaid.model.campaign.MilestoneStatus;
import com.crowdaid.model.voting.VoteType;
import com.crowdaid.service.MilestoneService;
import com.crowdaid.service.VoteService;
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

import java.time.format.DateTimeFormatter;

/**
 * Controller for voting on milestones (UC9: Vote on Milestone).
 */
public class VotingRequestsController {
    
    private static final Logger logger = LoggerFactory.getLogger(VotingRequestsController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    private final ViewLoader viewLoader;
    private final VoteService voteService;
    private final MilestoneService milestoneService;
    
    @FXML private TableView<Milestone> votingRequestsTable;
    @FXML private TableColumn<Milestone, String> campaignColumn;
    @FXML private TableColumn<Milestone, String> milestoneColumn;
    @FXML private TableColumn<Milestone, String> statusColumn;
    @FXML private TableColumn<Milestone, String> deadlineColumn;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private TextArea commentArea;
    @FXML private Button backButton;
    
    private ObservableList<Milestone> votingRequests;
    private Milestone selectedMilestone;
    
    public VotingRequestsController() {
        this.viewLoader = ViewLoader.getInstance();
        this.voteService = new VoteService();
        this.milestoneService = new MilestoneService();
        this.votingRequests = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        SessionManager.getInstance().getCurrentUser();
        
        // Setup table columns to display milestone data
        campaignColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty("Campaign #" + cellData.getValue().getCampaignId()));
        milestoneColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString()));
        deadlineColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getExpectedDate().format(DATE_FORMATTER)));
        
        votingRequestsTable.setItems(votingRequests);
        
        // Handle selection to track selected milestone
        votingRequestsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> selectedMilestone = newSelection);
        
        loadVotingRequests();
        
        logger.info("Voting requests screen initialized");
    }
    
    /**
     * Load milestones that are under review for voting.
     */
    private void loadVotingRequests() {
        try {
            votingRequests.clear();
            votingRequests.addAll(milestoneService.getMilestonesUnderReview());
            logger.info("Loaded {} milestones under review", votingRequests.size());
        } catch (Exception e) {
            logger.error("Error loading voting requests", e);
            AlertUtil.showError("Error", "Failed to load voting requests: " + e.getMessage());
        }
    }
    
    /**
     * Handle approve button click.
     */
    @FXML
    private void handleApprove(ActionEvent event) {
        Milestone selected = votingRequestsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a voting request.");
            return;
        }
        
        Long donorId = SessionManager.getInstance().getCurrentUser().getId();
        String comment = commentArea.getText();
        
        try {
            voteService.castVote(selected.getId(), donorId, VoteType.APPROVE, comment);
            AlertUtil.showSuccess("Vote Cast", "Your approval vote has been recorded successfully.");
            commentArea.clear();
            loadVotingRequests(); // Refresh list to remove voted milestone
        } catch (BusinessException e) {
            logger.error("Error casting approve vote", e);
            AlertUtil.showError("Voting Error", e.getMessage());
        } catch (Exception e) {
            logger.error("Error casting approve vote", e);
            AlertUtil.showError("Error", "Failed to cast vote: " + e.getMessage());
        }
    }
    
    /**
     * Handle reject button click.
     */
    @FXML
    private void handleReject(ActionEvent event) {
        Milestone selected = votingRequestsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a voting request.");
            return;
        }
        
        String comment = commentArea.getText();
        
        if (comment == null || comment.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Please provide a reason for rejection.");
            return;
        }
        
        Long donorId = SessionManager.getInstance().getCurrentUser().getId();
        
        try {
            voteService.castVote(selected.getId(), donorId, VoteType.REJECT, comment);
            AlertUtil.showSuccess("Vote Cast", "Your rejection vote has been recorded successfully.");
            commentArea.clear();
            loadVotingRequests(); // Refresh list to remove voted milestone
        } catch (BusinessException e) {
            logger.error("Error casting reject vote", e);
            AlertUtil.showError("Voting Error", e.getMessage());
        } catch (Exception e) {
            logger.error("Error casting reject vote", e);
            AlertUtil.showError("Error", "Failed to cast vote: " + e.getMessage());
        }
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
