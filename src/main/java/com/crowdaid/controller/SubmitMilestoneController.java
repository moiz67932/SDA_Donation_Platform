package com.crowdaid.controller;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.Evidence;
import com.crowdaid.model.campaign.Milestone;
import com.crowdaid.model.campaign.MilestoneStatus;
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
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
    private List<File> evidenceFiles;
    private static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
    
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
        
        // Check if a specific milestone was pre-selected from Manage Milestones screen
        Milestone preSelectedMilestone = (Milestone) SessionManager.getInstance()
            .getAttribute("selectedMilestoneForSubmission");
        
        if (preSelectedMilestone != null) {
            // Pre-select the milestone in the dropdown
            milestoneComboBox.setValue(preSelectedMilestone);
            // Clear the session attribute
            SessionManager.getInstance().removeAttribute("selectedMilestoneForSubmission");
            logger.info("Pre-selected milestone: {}", preSelectedMilestone.getTitle());
        }
        
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
                // Only show PENDING or REJECTED milestones (ready to submit)
                for (Milestone m : campaignMilestones) {
                    MilestoneStatus status = m.getStatus();
                    if (status == MilestoneStatus.PENDING || status == MilestoneStatus.REJECTED) {
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
     * Handles choose image buttons.
     */
    @FXML
    private void handleChooseImage1(ActionEvent event) {
        chooseImageFile(evidence1Field, 0);
    }
    
    @FXML
    private void handleChooseImage2(ActionEvent event) {
        chooseImageFile(evidence2Field, 1);
    }
    
    @FXML
    private void handleChooseImage3(ActionEvent event) {
        chooseImageFile(evidence3Field, 2);
    }
    
    /**
     * Handles clear image buttons.
     */
    @FXML
    private void handleClearImage1(ActionEvent event) {
        clearImageFile(evidence1Field, 0);
    }
    
    @FXML
    private void handleClearImage2(ActionEvent event) {
        clearImageFile(evidence2Field, 1);
    }
    
    @FXML
    private void handleClearImage3(ActionEvent event) {
        clearImageFile(evidence3Field, 2);
    }
    
    /**
     * Opens file chooser to select an image file.
     */
    private void chooseImageFile(TextField field, int index) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Evidence Image");
        
        // Set file extension filters
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
            "Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp");
        fileChooser.getExtensionFilters().add(imageFilter);
        
        File selectedFile = fileChooser.showOpenDialog(viewLoader.getPrimaryStage());
        
        if (selectedFile != null) {
            if (isValidImageFile(selectedFile)) {
                field.setText(selectedFile.getName());
                
                // Update or add to evidence files list
                if (index < evidenceFiles.size()) {
                    evidenceFiles.set(index, selectedFile);
                } else {
                    // Fill any gaps with null
                    while (evidenceFiles.size() < index) {
                        evidenceFiles.add(null);
                    }
                    evidenceFiles.add(selectedFile);
                }
                
                updateEvidenceListView();
                logger.info("Selected evidence image: {}", selectedFile.getAbsolutePath());
            } else {
                AlertUtil.showError("Invalid File", "Please select a valid image file (jpg, jpeg, png, gif, bmp).");
            }
        }
    }
    
    /**
     * Clears the selected image file.
     */
    private void clearImageFile(TextField field, int index) {
        field.clear();
        field.setPromptText(index == 0 ? "No file selected" : "No file selected (optional)");
        
        if (index < evidenceFiles.size()) {
            evidenceFiles.set(index, null);
            updateEvidenceListView();
        }
        
        logger.info("Cleared evidence image at index {}", index);
    }
    
    /**
     * Validates if the file is a valid image.
     */
    private boolean isValidImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        for (String ext : ALLOWED_IMAGE_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Updates the evidence list view with selected files.
     */
    private void updateEvidenceListView() {
        List<String> fileNames = new ArrayList<>();
        for (int i = 0; i < evidenceFiles.size(); i++) {
            File file = evidenceFiles.get(i);
            if (file != null) {
                fileNames.add((i + 1) + ". " + file.getName());
            }
        }
        evidenceListView.setItems(FXCollections.observableArrayList(fileNames));
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
        
        // Get only non-null evidence files
        List<File> validEvidenceFiles = new ArrayList<>();
        for (File file : evidenceFiles) {
            if (file != null) {
                validEvidenceFiles.add(file);
            }
        }
        
        if (validEvidenceFiles.isEmpty()) {
            AlertUtil.showWarning("No Evidence", "Please upload at least one evidence image.");
            return;
        }
        
        try {
            // Create Evidence objects from selected files
            List<Evidence> evidenceList = new ArrayList<>();
            for (File file : validEvidenceFiles) {
                Evidence evidence = new Evidence();
                evidence.setDescription("Evidence: " + file.getName());
                evidence.setFilePath(file.getAbsolutePath());
                evidenceList.add(evidence);
            }
            
            // Submit milestone completion with evidence
            milestoneService.submitMilestoneCompletion(
                    selectedMilestone.getId(), 
                    evidenceList,
                    completionDesc
            );
            
            AlertUtil.showInfo("Success", 
                    "Milestone submitted for voting! Donors can now review your evidence and vote.");
            
            logger.info("Milestone submitted for review: id={}, evidenceCount={}", 
                       selectedMilestone.getId(), evidenceList.size());
            
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
