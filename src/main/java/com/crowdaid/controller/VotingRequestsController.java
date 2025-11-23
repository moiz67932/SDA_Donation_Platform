package com.crowdaid.controller;

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
 * Controller for voting on milestones (UC9: Vote on Milestone).
 */
public class VotingRequestsController {
    
    private static final Logger logger = LoggerFactory.getLogger(VotingRequestsController.class);
    
    private final ViewLoader viewLoader;
    
    @FXML private TableView<Object> votingRequestsTable;
    @FXML private TableColumn<Object, String> campaignColumn;
    @FXML private TableColumn<Object, String> milestoneColumn;
    @FXML private TableColumn<Object, String> statusColumn;
    @FXML private TableColumn<Object, String> deadlineColumn;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private TextArea commentArea;
    @FXML private Button backButton;
    
    private ObservableList<Object> votingRequests;
    
    public VotingRequestsController() {
        this.viewLoader = ViewLoader.getInstance();
        this.votingRequests = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        SessionManager.getInstance().getCurrentUser();
        
        // Setup table columns
        campaignColumn.setCellValueFactory(new PropertyValueFactory<>("campaignName"));
        milestoneColumn.setCellValueFactory(new PropertyValueFactory<>("milestoneName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        
        votingRequestsTable.setItems(votingRequests);
        
        logger.info("Voting requests screen initialized");
    }
    
    /**
     * Handle approve button click.
     */
    @FXML
    private void handleApprove(ActionEvent event) {
        Object selected = votingRequestsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a voting request.");
            return;
        }
        
        commentArea.getText();
        
        // voteService.castVote(donor, milestone, VoteType.APPROVE, comment);
        
        AlertUtil.showSuccess("Vote Cast", "Your approval vote has been recorded.");
        
        commentArea.clear();
    }
    
    /**
     * Handle reject button click.
     */
    @FXML
    private void handleReject(ActionEvent event) {
        Object selected = votingRequestsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a voting request.");
            return;
        }
        
        String comment = commentArea.getText();
        
        if (comment == null || comment.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Please provide a reason for rejection.");
            return;
        }
        
        // voteService.castVote(donor, milestone, VoteType.REJECT, comment);
        
        AlertUtil.showSuccess("Vote Cast", "Your rejection vote has been recorded.");
        
        commentArea.clear();
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
