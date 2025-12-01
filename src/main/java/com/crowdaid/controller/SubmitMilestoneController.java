package com.crowdaid.controller;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.Milestone;
import com.crowdaid.model.user.Campaigner;
import com.crowdaid.model.user.User;
import com.crowdaid.service.CampaignService;
import com.crowdaid.service.MilestoneService;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for submitting milestone completion.
 * Handles UC5 (Submit Milestone Completion).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class SubmitMilestoneController {
    
    private static final Logger logger = LoggerFactory.getLogger(SubmitMilestoneController.class);
    
    private final MilestoneService milestoneService;
    private final CampaignService campaignService;
    private final ViewLoader viewLoader;
    private Campaigner currentCampaigner;
    private List<String> evidenceFiles;
    
    @FXML private ComboBox<Milestone> milestoneComboBox;
    @FXML private TextArea milestoneDetailsArea;
    @FXML private TextArea completionDescriptionArea;
    @FXML private TextField evidence1Field;
    @FXML private TextField evidence2Field;
    @FXML private TextField evidence3Field;
    @FXML private ListView<String> evidenceListView;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    
    public SubmitMilestoneController() {
        this.milestoneService = new MilestoneService();
        this.campaignService = new CampaignService();
        this.viewLoader = ViewLoader.getInstance();
        this.evidenceFiles = new ArrayList<>();
    }
    
    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        
        if (user == null || !(user instanceof Campaigner)) {
            AlertUtil.showError("Access Denied", "You must be logged in as a campaigner.");
            viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
            return;
        }
        
        currentCampaigner = (Campaigner) user;
        
        loadMilestones();
        setupListeners();
        
        logger.info("Submit Milestone loaded for campaigner: {}", currentCampaigner.getEmail());
    }
    
    /**
     * Loads milestones available for submission.
     */
    private void loadMilestones() {
        try {
            List<Campaign> campaigns = campaignService.getCampaignerCampaigns(currentCampaigner.getId());
            List<Milestone> allMilestones = new ArrayList<>();
            
            for (Campaign campaign : campaigns) {
                List<Milestone> campaignMilestones = milestoneService.getCampaignMilestones(campaign.getId());
                // Only show PLANNED milestones (ready to submit)
                for (Milestone m : campaignMilestones) {
                    if (m.getStatus().toString().equals("PLANNED")) {
                        allMilestones.add(m);
                    }
                }
            }
            
            milestoneComboBox.setItems(FXCollections.observableArrayList(allMilestones));
            
        } catch (BusinessException e) {
            logger.error("Error loading milestones", e);
            AlertUtil.showError("Error", "Failed to load milestones: " + e.getMessage());
        }
    }
    
    /**
     * Sets up change listeners.
     */
    private void setupListeners() {
        milestoneComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayMilestoneDetails(newVal);
            }
        });
    }
    
    /**
     * Displays selected milestone details.
     */
    private void displayMilestoneDetails(Milestone milestone) {
        String details = "Title: " + milestone.getTitle() + "\n\n" +
                        "Description: " + milestone.getDescription() + "\n\n" +
                        "Fund Amount: $" + milestone.getFundAmount() + "\n" +
                        "Expected Date: " + milestone.getExpectedDate();
        milestoneDetailsArea.setText(details);
    }
    
    /**
     * Handles add evidence buttons.
     */
    @FXML
    private void handleAddEvidence1(ActionEvent event) {
        addEvidence(evidence1Field);
    }
    
    @FXML
    private void handleAddEvidence2(ActionEvent event) {
        addEvidence(evidence2Field);
    }
    
    @FXML
    private void handleAddEvidence3(ActionEvent event) {
        addEvidence(evidence3Field);
    }
    
    /**
     * Adds evidence file path to the list.
     */
    private void addEvidence(TextField field) {
        String filePath = field.getText().trim();
        
        if (filePath.isEmpty()) {
            AlertUtil.showWarning("Empty Field", "Please enter a file path.");
            return;
        }
        
        evidenceFiles.add(filePath);
        evidenceListView.setItems(FXCollections.observableArrayList(evidenceFiles));
        field.clear();
        
        logger.info("Added evidence file: {}", filePath);
    }
    
    /**
     * Handles submit button click (UC5: Submit Milestone Completion).
     */
    @FXML
    private void handleSubmit(ActionEvent event) {
        Milestone selectedMilestone = milestoneComboBox.getValue();
        String completionDesc = completionDescriptionArea.getText().trim();
        
        if (selectedMilestone == null) {
            AlertUtil.showWarning("No Milestone Selected", "Please select a milestone.");
            return;
        }
        
        if (completionDesc.isEmpty()) {
            AlertUtil.showWarning("Missing Description", "Please provide a completion description.");
            return;
        }
        
        if (evidenceFiles.isEmpty()) {
            AlertUtil.showWarning("No Evidence", "Please add at least one evidence file.");
            return;
        }
        
        try {
            // Submit milestone completion - combine evidence files into single string
            String evidenceDescription = String.join(", ", evidenceFiles);
            
            milestoneService.submitMilestoneCompletion(
                    selectedMilestone.getId(), 
                    evidenceDescription, 
                    completionDesc
            );
            
            AlertUtil.showInfo("Success", 
                    "Milestone submitted for review! Donors will be notified to vote.");
            
            logger.info("Milestone submitted for review: id={}", selectedMilestone.getId());
            
            viewLoader.loadView(viewLoader.getPrimaryStage(), 
                    "/fxml/campaigner_dashboard.fxml", "CrowdAid - Campaigner Dashboard");
            
        } catch (com.crowdaid.exception.ValidationException e) {
            logger.error("Validation error submitting milestone", e);
            AlertUtil.showError("Validation Error", e.getMessage());
        } catch (BusinessException e) {
            logger.error("Error submitting milestone", e);
            AlertUtil.showError("Submission Failed", e.getMessage());
        }
    }
    
    /**
     * Handles cancel button click.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
                "/fxml/campaigner_dashboard.fxml", "CrowdAid - Campaigner Dashboard");
    }
}
