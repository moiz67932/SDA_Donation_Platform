package com.crowdaid.controller;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.model.campaign.Evidence;
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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    @FXML private ListView<String> evidenceListView;
    @FXML private TextArea evidenceDetailsArea;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private TextArea commentArea;
    @FXML private Button backButton;
    
    private ObservableList<Milestone> votingRequests;
    private Milestone selectedMilestone;
    private List<Evidence> currentEvidenceList;
    
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
        
        // Handle selection to track selected milestone and load evidence
        votingRequestsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                selectedMilestone = newSelection;
                if (newSelection != null) {
                    loadMilestoneEvidence(newSelection.getId());
                } else {
                    clearEvidenceDisplay();
                }
            });
        
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
     * Load evidence for the selected milestone.
     */
    private void loadMilestoneEvidence(Long milestoneId) {
        try {
            List<Evidence> evidenceList = milestoneService.getMilestoneEvidence(milestoneId);
            currentEvidenceList = evidenceList; // Store for image viewing
            
            ObservableList<String> evidenceFileNames = FXCollections.observableArrayList();
            StringBuilder detailsBuilder = new StringBuilder();
            
            if (evidenceList.isEmpty()) {
                evidenceFileNames.add("No evidence uploaded");
                detailsBuilder.append("No evidence available for this milestone.");
            } else {
                for (int i = 0; i < evidenceList.size(); i++) {
                    Evidence evidence = evidenceList.get(i);
                    File file = new File(evidence.getFilePath());
                    evidenceFileNames.add((i + 1) + ". " + file.getName());
                    
                    detailsBuilder.append("Evidence ").append(i + 1).append(":\n");
                    detailsBuilder.append("File: ").append(file.getName()).append("\n");
                    detailsBuilder.append("Path: ").append(evidence.getFilePath()).append("\n");
                    detailsBuilder.append("Description: ").append(evidence.getDescription()).append("\n");
                    detailsBuilder.append("Uploaded: ").append(evidence.getCreatedAt().format(
                        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("\n\n");
                }
            }
            
            evidenceListView.setItems(evidenceFileNames);
            evidenceDetailsArea.setText(detailsBuilder.toString());
            
            logger.info("Loaded {} evidence items for milestone {}", evidenceList.size(), milestoneId);
            
        } catch (Exception e) {
            logger.error("Error loading milestone evidence", e);
            evidenceListView.setItems(FXCollections.observableArrayList("Error loading evidence"));
            evidenceDetailsArea.setText("Failed to load evidence: " + e.getMessage());
        }
    }
    
    /**
     * Clear evidence display when no milestone is selected.
     */
    private void clearEvidenceDisplay() {
        evidenceListView.setItems(FXCollections.observableArrayList());
        evidenceDetailsArea.clear();
        currentEvidenceList = null;
    }
    
    /**
     * Handle evidence list item click (double-click to view image).
     */
    @FXML
    private void handleEvidenceClick(MouseEvent event) {
        if (event.getClickCount() == 2) { // Double-click
            handleViewImage(null);
        }
    }
    
    /**
     * Handle view image button click.
     */
    @FXML
    private void handleViewImage(ActionEvent event) {
        int selectedIndex = evidenceListView.getSelectionModel().getSelectedIndex();
        
        if (selectedIndex < 0) {
            AlertUtil.showWarning("No Selection", "Please select an evidence file to view.");
            return;
        }
        
        if (currentEvidenceList == null || selectedIndex >= currentEvidenceList.size()) {
            AlertUtil.showError("Error", "Evidence not available.");
            return;
        }
        
        Evidence selectedEvidence = currentEvidenceList.get(selectedIndex);
        File imageFile = new File(selectedEvidence.getFilePath());
        
        if (!imageFile.exists()) {
            AlertUtil.showError("File Not Found", 
                "The evidence image file could not be found:\n" + selectedEvidence.getFilePath());
            return;
        }
        
        try {
            // Create image view window
            Stage imageStage = new Stage();
            imageStage.initModality(Modality.APPLICATION_MODAL);
            imageStage.setTitle("Evidence Image - " + imageFile.getName());
            
            // Load and display image
            Image image = new Image(imageFile.toURI().toString());
            ImageView imageView = new ImageView(image);
            
            // Preserve aspect ratio and set max size
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(800);
            imageView.setFitHeight(600);
            
            StackPane pane = new StackPane(imageView);
            Scene scene = new Scene(pane);
            
            imageStage.setScene(scene);
            imageStage.setResizable(true);
            imageStage.show();
            
            logger.info("Displaying evidence image: {}", imageFile.getName());
            
        } catch (Exception e) {
            logger.error("Error displaying image", e);
            AlertUtil.showError("Display Error", 
                "Failed to display the image. The file may be corrupted or in an unsupported format.\n" + 
                e.getMessage());
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
