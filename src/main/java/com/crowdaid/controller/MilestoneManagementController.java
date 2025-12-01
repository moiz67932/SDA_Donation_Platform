package com.crowdaid.controller;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.Milestone;
import com.crowdaid.model.campaign.MilestoneStatus;
import com.crowdaid.repository.interfaces.MilestoneRepository;
import com.crowdaid.repository.mysql.MySQLMilestoneRepository;
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
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for managing campaign milestones (UC4: Set Milestones).
 * Allows campaigners to add, view, and delete milestones for their campaigns.
 */
public class MilestoneManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(MilestoneManagementController.class);
    
    private final ViewLoader viewLoader;
    private final MilestoneRepository milestoneRepository;
    private Campaign selectedCampaign;
    
    @FXML private Button backButton;
    @FXML private Label campaignTitleLabel;
    
    // Add Milestone Fields
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField amountField;
    @FXML private DatePicker expectedDatePicker;
    @FXML private Button addMilestoneButton;
    @FXML private Button clearButton;
    
    // Milestones Table
    @FXML private TableView<Milestone> milestonesTable;
    @FXML private TableColumn<Milestone, String> milestoneTitleColumn;
    @FXML private TableColumn<Milestone, Double> milestoneAmountColumn;
    @FXML private TableColumn<Milestone, LocalDate> milestoneExpectedDateColumn;
    @FXML private TableColumn<Milestone, MilestoneStatus> milestoneStatusColumn;
    @FXML private Button viewMilestoneButton;
    @FXML private Button submitForVotingButton;
    @FXML private Button deleteMilestoneButton;
    
    private ObservableList<Milestone> milestonesList;
    
    public MilestoneManagementController() {
        this.viewLoader = ViewLoader.getInstance();
        this.milestoneRepository = new MySQLMilestoneRepository();
        this.milestonesList = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        SessionManager.getInstance().getCurrentUser();
        
        // Get selected campaign from session
        selectedCampaign = (Campaign) SessionManager.getInstance().getAttribute("selectedCampaign");
        
        if (selectedCampaign != null) {
            campaignTitleLabel.setText("Manage Milestones - " + selectedCampaign.getTitle());
            setupTable();
            loadMilestones();
        } else {
            AlertUtil.showError("Error", "No campaign selected. Please select a campaign first.");
            handleBack(null);
        }
        
        logger.info("Milestone management screen initialized for campaign: {}", 
                   selectedCampaign != null ? selectedCampaign.getTitle() : "none");
    }
    
    /**
     * Setup the milestones table columns.
     */
    private void setupTable() {
        milestoneTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        milestoneAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        milestoneExpectedDateColumn.setCellValueFactory(new PropertyValueFactory<>("expectedDate"));
        milestoneStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        milestonesTable.setItems(milestonesList);
    }
    
    /**
     * Load milestones for the selected campaign.
     */
    private void loadMilestones() {
        try {
            List<Milestone> milestones = milestoneRepository.findByCampaign(selectedCampaign.getId());
            milestonesList.clear();
            milestonesList.addAll(milestones);
            
            logger.info("Loaded {} milestones for campaign: {}", milestones.size(), selectedCampaign.getTitle());
        } catch (SQLException e) {
            logger.error("Error loading milestones", e);
            AlertUtil.showError("Database Error", "Failed to load milestones.");
        }
    }
    
    /**
     * Handle add milestone button click.
     */
    @FXML
    private void handleAddMilestone(ActionEvent event) {
        String title = titleField.getText();
        String description = descriptionField.getText();
        String amountStr = amountField.getText();
        LocalDate expectedDate = expectedDatePicker.getValue();
        
        // Validation
        if (title == null || title.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Milestone title is required.");
            return;
        }
        
        if (description == null || description.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Milestone description is required.");
            return;
        }
        
        if (amountStr == null || amountStr.trim().isEmpty()) {
            AlertUtil.showError("Validation Error", "Milestone amount is required.");
            return;
        }
        
        if (expectedDate == null) {
            AlertUtil.showError("Validation Error", "Expected date is required.");
            return;
        }
        
        try {
            double amount = Double.parseDouble(amountStr);
            
            if (amount <= 0) {
                AlertUtil.showError("Validation Error", "Amount must be positive.");
                return;
            }
            
            // Validate that expected date is in the future
            if (expectedDate.isBefore(LocalDate.now())) {
                AlertUtil.showError("Validation Error", "Expected date must be in the future.");
                return;
            }
            
            // Validate that expected date is not after campaign end date
            if (selectedCampaign.getEndDate() != null && expectedDate.isAfter(selectedCampaign.getEndDate())) {
                AlertUtil.showError("Validation Error", 
                    String.format("Milestone expected date (%s) cannot be after campaign end date (%s).",
                                expectedDate, selectedCampaign.getEndDate()));
                return;
            }
            
            // Validate that milestone amount doesn't cause total to exceed campaign goal
            double totalExistingAmount = milestonesList.stream()
                .mapToDouble(Milestone::getAmount)
                .sum();
            
            if (totalExistingAmount + amount > selectedCampaign.getGoalAmount()) {
                AlertUtil.showError("Validation Error", 
                    String.format("Total milestone amounts ($%.2f) would exceed campaign goal ($%.2f).",
                                totalExistingAmount + amount, selectedCampaign.getGoalAmount()));
                return;
            }
            
            // Create and save milestone
            Milestone milestone = new Milestone(selectedCampaign.getId(), title, description, amount, expectedDate);
            milestone.setStatus(MilestoneStatus.PENDING);
            
            Milestone savedMilestone = milestoneRepository.save(milestone);
            
            if (savedMilestone != null) {
                AlertUtil.showSuccess("Milestone Added", 
                    "Milestone has been successfully added to your campaign.");
                
                // Clear form
                handleClear(null);
                
                // Reload milestones
                loadMilestones();
                
                logger.info("Milestone added: {} for campaign: {}", title, selectedCampaign.getTitle());
            } else {
                AlertUtil.showError("Error", "Failed to add milestone.");
            }
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Please enter a valid amount.");
        } catch (SQLException e) {
            logger.error("Error adding milestone", e);
            AlertUtil.showError("Database Error", "Failed to add milestone.");
        }
    }
    
    /**
     * Handle clear button click.
     */
    @FXML
    private void handleClear(ActionEvent event) {
        titleField.clear();
        descriptionField.clear();
        amountField.clear();
        expectedDatePicker.setValue(null);
    }
    
    /**
     * Handle submit for voting button click (UC5: Submit Milestone for Voting).
     */
    @FXML
    private void handleSubmitForVoting(ActionEvent event) {
        Milestone selectedMilestone = milestonesTable.getSelectionModel().getSelectedItem();
        
        if (selectedMilestone == null) {
            AlertUtil.showError("No Selection", "Please select a milestone to submit for voting.");
            return;
        }
        
        // Check if milestone is in a state that can be submitted
        if (selectedMilestone.getStatus() != MilestoneStatus.PENDING && 
            selectedMilestone.getStatus() != MilestoneStatus.REJECTED) {
            AlertUtil.showWarning("Invalid Status", 
                "Only pending or rejected milestones can be submitted for voting.\\nCurrent status: " + 
                selectedMilestone.getStatus());
            return;
        }
        
        // Confirm submission
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Submit for Voting");
        confirmAlert.setHeaderText("Submit Milestone for Donor Voting");
        confirmAlert.setContentText(
            "This will submit the milestone to donors for voting.\\n" +
            "Donors who contributed to this campaign will be able to vote " +
            "to approve or reject the milestone completion.\\n\\n" +
            "Are you sure you want to proceed?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Update milestone status to UNDER_REVIEW
                    selectedMilestone.setStatus(MilestoneStatus.UNDER_REVIEW);
                    milestoneRepository.update(selectedMilestone);
                    
                    AlertUtil.showSuccess("Submitted for Voting", 
                        "Milestone has been submitted for voting.\\n" +
                        "Donors will now be able to vote on this milestone completion.");
                    
                    // Reload milestones
                    loadMilestones();
                    
                    logger.info("Milestone submitted for voting: {} from campaign: {}", 
                               selectedMilestone.getTitle(), selectedCampaign.getTitle());
                    
                } catch (SQLException e) {
                    logger.error("Error submitting milestone for voting", e);
                    AlertUtil.showError("Database Error", "Failed to submit milestone for voting.");
                }
            }
        });
    }
    
    /**
     * Handle view milestone button click.
     */
    @FXML
    private void handleViewMilestone(ActionEvent event) {
        Milestone selectedMilestone = milestonesTable.getSelectionModel().getSelectedItem();
        
        if (selectedMilestone == null) {
            AlertUtil.showError("No Selection", "Please select a milestone to view.");
            return;
        }
        
        // Show milestone details in a dialog
        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(selectedMilestone.getTitle()).append("\n\n");
        details.append("Description: ").append(selectedMilestone.getDescription()).append("\n\n");
        details.append("Amount: $").append(String.format("%.2f", selectedMilestone.getAmount())).append("\n");
        details.append("Expected Date: ").append(selectedMilestone.getExpectedDate()).append("\n");
        details.append("Status: ").append(selectedMilestone.getStatus()).append("\n");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Milestone Details");
        alert.setHeaderText(selectedMilestone.getTitle());
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
    
    /**
     * Handle delete milestone button click.
     */
    @FXML
    private void handleDeleteMilestone(ActionEvent event) {
        Milestone selectedMilestone = milestonesTable.getSelectionModel().getSelectedItem();
        
        if (selectedMilestone == null) {
            AlertUtil.showError("No Selection", "Please select a milestone to delete.");
            return;
        }
        
        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Milestone");
        confirmAlert.setContentText("Are you sure you want to delete this milestone?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    milestoneRepository.delete(selectedMilestone.getId());
                    
                    AlertUtil.showSuccess("Milestone Deleted", "Milestone has been successfully deleted.");
                    
                    // Reload milestones
                    loadMilestones();
                    
                    logger.info("Milestone deleted: {} from campaign: {}", 
                               selectedMilestone.getTitle(), selectedCampaign.getTitle());
                    
                } catch (SQLException e) {
                    logger.error("Error deleting milestone", e);
                    AlertUtil.showError("Database Error", "Failed to delete milestone.");
                }
            }
        });
    }
    
    /**
     * Handle back button click.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        // Clear selected campaign from session
        SessionManager.getInstance().removeAttribute("selectedCampaign");
        
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/campaigner_dashboard.fxml", "CrowdAid - Campaigner Dashboard");
    }
}
