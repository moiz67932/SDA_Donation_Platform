package com.crowdaid.service;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.Evidence;
import com.crowdaid.model.campaign.Milestone;
import com.crowdaid.model.campaign.MilestoneStatus;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.interfaces.EvidenceRepository;
import com.crowdaid.repository.interfaces.MilestoneRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.repository.mysql.MySQLEvidenceRepository;
import com.crowdaid.repository.mysql.MySQLMilestoneRepository;
import com.crowdaid.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * MilestoneService handles milestone management operations.
 * 
 * Implements UC4 (Define Milestones) and UC5 (Submit Milestone Completion).
 * 
 * Applies GRASP principles:
 * - Information Expert: Milestone-related business logic
 * - Low Coupling: Depends on repository interfaces
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MilestoneService {
    
    private static final Logger logger = LoggerFactory.getLogger(MilestoneService.class);
    private final MilestoneRepository milestoneRepository;
    private final CampaignRepository campaignRepository;
    private final EvidenceRepository evidenceRepository;
    
    /**
     * Constructor initializing repositories.
     */
    public MilestoneService() {
        this.milestoneRepository = new MySQLMilestoneRepository();
        this.campaignRepository = new MySQLCampaignRepository();
        this.evidenceRepository = new MySQLEvidenceRepository();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param milestoneRepository the milestone repository
     * @param campaignRepository the campaign repository
     */
    public MilestoneService(MilestoneRepository milestoneRepository, CampaignRepository campaignRepository) {
        this.milestoneRepository = milestoneRepository;
        this.campaignRepository = campaignRepository;
        this.evidenceRepository = new MySQLEvidenceRepository();
    }
    
    /**
     * Defines a new milestone for a campaign (UC4: Define Milestones).
     * 
     * @param campaignId the campaign ID
     * @param title the milestone title
     * @param description the milestone description
     * @param amount the milestone amount
     * @param expectedDate the expected completion date
     * @return the created milestone
     * @throws ValidationException if validation fails
     * @throws BusinessException if creation fails
     */
    public Milestone defineMilestone(Long campaignId, String title, String description,
                                    double amount, LocalDate expectedDate)
            throws ValidationException, BusinessException {
        
        // Validate inputs
        Validator.validatePositive(campaignId, "Campaign ID");
        Validator.validateNonEmpty(title, "Milestone title");
        Validator.validateNonEmpty(description, "Milestone description");
        Validator.validatePositive(amount, "Milestone amount");
        Validator.validateNotNull(expectedDate, "Expected date");
        
        if (expectedDate.isBefore(LocalDate.now())) {
            throw new ValidationException("Expected date cannot be in the past");
        }
        
        try {
            // Verify campaign exists and is owned by the campaigner
            Campaign campaign = campaignRepository.findById(campaignId);
            
            if (campaign == null) {
                throw new BusinessException("Campaign not found");
            }
            
            // Validate that milestone expected date is not after campaign end date
            if (campaign.getEndDate() != null && expectedDate.isAfter(campaign.getEndDate())) {
                throw new ValidationException(
                    String.format("Milestone expected date (%s) cannot be after campaign end date (%s)",
                                expectedDate, campaign.getEndDate()));
            }
            
            // Validate total milestone amounts don't exceed campaign goal
            List<Milestone> existingMilestones = milestoneRepository.findByCampaign(campaignId);
            double totalMilestoneAmount = existingMilestones.stream()
                    .mapToDouble(Milestone::getAmount)
                    .sum() + amount;
            
            if (totalMilestoneAmount > campaign.getGoalAmount()) {
                throw new BusinessException(
                    String.format("Total milestone amounts (%.2f) cannot exceed campaign goal (%.2f)",
                                totalMilestoneAmount, campaign.getGoalAmount()));
            }
            
            // Create milestone
            Milestone milestone = new Milestone(campaignId, title, description, amount, expectedDate);
            milestone.setStatus(MilestoneStatus.PENDING);
            
            Milestone savedMilestone = milestoneRepository.save(milestone);
            
            logger.info("Milestone created: id={}, campaignId={}, title={}, amount={}", 
                       savedMilestone.getId(), campaignId, title, amount);
            
            return savedMilestone;
            
        } catch (SQLException e) {
            logger.error("Database error while creating milestone", e);
            throw new BusinessException("Failed to create milestone", e);
        }
    }
    
    /**
     * Retrieves a milestone by ID.
     * 
     * @param milestoneId the milestone ID
     * @return the milestone
     * @throws ValidationException if validation fails
     * @throws BusinessException if milestone not found
     */
    public Milestone getMilestoneById(Long milestoneId) throws ValidationException, BusinessException {
        Validator.validatePositive(milestoneId, "Milestone ID");
        
        try {
            Milestone milestone = milestoneRepository.findById(milestoneId);
            
            if (milestone == null) {
                logger.warn("Milestone not found: id={}", milestoneId);
                throw new BusinessException("Milestone not found with ID: " + milestoneId);
            }
            
            logger.debug("Milestone retrieved: id={}, title={}", milestone.getId(), milestone.getTitle());
            return milestone;
            
        } catch (SQLException e) {
            logger.error("Database error while finding milestone", e);
            throw new BusinessException("Failed to retrieve milestone", e);
        }
    }
    
    /**
     * Retrieves all milestones for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return list of milestones
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Milestone> getMilestonesByCampaign(Long campaignId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        
        try {
            List<Milestone> milestones = milestoneRepository.findByCampaign(campaignId);
            logger.debug("Retrieved {} milestones for campaign {}", milestones.size(), campaignId);
            return milestones;
            
        } catch (SQLException e) {
            logger.error("Database error while finding milestones by campaign", e);
            throw new BusinessException("Failed to retrieve milestones", e);
        }
    }
    
    /**
     * Gets all milestones for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return list of milestones
     * @throws BusinessException if operation fails
     */
    public List<Milestone> getCampaignMilestones(Long campaignId) throws BusinessException {
        try {
            return milestoneRepository.findByCampaign(campaignId);
        } catch (SQLException e) {
            logger.error("Error retrieving milestones for campaign: campaignId={}", campaignId, e);
            throw new BusinessException("Failed to retrieve milestones", e);
        }
    }
    
    /**
     * Submits milestone completion with evidence (UC5: Submit Milestone Completion).
     * 
     * @param milestoneId the milestone ID
     * @param evidenceList the list of evidence objects with file paths and descriptions
     * @param completionDescription the completion description
     * @throws ValidationException if validation fails
     * @throws BusinessException if submission fails
     */
    public void submitMilestoneCompletion(Long milestoneId, List<Evidence> evidenceList, 
                                         String completionDescription)
            throws ValidationException, BusinessException {
        
        Validator.validatePositive(milestoneId, "Milestone ID");
        Validator.validateNotNull(evidenceList, "Evidence list");
        Validator.validateNonEmpty(completionDescription, "Completion description");
        
        if (evidenceList.isEmpty()) {
            throw new ValidationException("At least one evidence item is required");
        }
        
        try {
            Milestone milestone = milestoneRepository.findById(milestoneId);
            
            if (milestone == null) {
                throw new BusinessException("Milestone not found");
            }
            
            if (milestone.getStatus() != MilestoneStatus.PENDING) {
                throw new BusinessException("Can only submit evidence for pending milestones. Current status: " + 
                                          milestone.getStatus());
            }
            
            // Save all evidence items to database
            for (Evidence evidence : evidenceList) {
                evidence.setMilestoneId(milestoneId);
                evidenceRepository.save(evidence);
            }
            
            // Update milestone status to under review
            milestone.setStatus(MilestoneStatus.UNDER_REVIEW);
            milestoneRepository.update(milestone);
            
            logger.info("Milestone completion submitted: id={}, campaignId={}, evidenceCount={}", 
                       milestoneId, milestone.getCampaignId(), evidenceList.size());
            
        } catch (SQLException e) {
            logger.error("Database error while submitting milestone completion", e);
            throw new BusinessException("Failed to submit milestone completion", e);
        }
    }
    
    /**
     * Retrieves evidence for a milestone.
     * 
     * @param milestoneId the milestone ID
     * @return list of evidence
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Evidence> getMilestoneEvidence(Long milestoneId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(milestoneId, "Milestone ID");
        
        try {
            List<Evidence> evidenceList = evidenceRepository.findByMilestone(milestoneId);
            logger.debug("Retrieved {} evidence items for milestone {}", evidenceList.size(), milestoneId);
            return evidenceList;
            
        } catch (SQLException e) {
            logger.error("Database error while retrieving evidence", e);
            throw new BusinessException("Failed to retrieve evidence", e);
        }
    }
    
    /**
     * Retrieves milestones under review (for voting).
     * 
     * @return list of milestones under review
     * @throws BusinessException if retrieval fails
     */
    public List<Milestone> getMilestonesUnderReview() throws BusinessException {
        try {
            List<Milestone> milestones = milestoneRepository.findUnderReview();
            logger.debug("Retrieved {} milestones under review", milestones.size());
            return milestones;
            
        } catch (SQLException e) {
            logger.error("Database error while finding milestones under review", e);
            throw new BusinessException("Failed to retrieve milestones", e);
        }
    }
    
    /**
     * Approves a milestone (called after successful voting or admin approval).
     * 
     * @param milestoneId the milestone ID
     * @throws ValidationException if validation fails
     * @throws BusinessException if approval fails
     */
    public void approveMilestone(Long milestoneId) throws ValidationException, BusinessException {
        Validator.validatePositive(milestoneId, "Milestone ID");
        
        try {
            Milestone milestone = milestoneRepository.findById(milestoneId);
            
            if (milestone == null) {
                throw new BusinessException("Milestone not found");
            }
            
            if (milestone.getStatus() != MilestoneStatus.UNDER_REVIEW) {
                throw new BusinessException("Can only approve milestones under review");
            }
            
            milestone.setStatus(MilestoneStatus.APPROVED);
            milestoneRepository.update(milestone);
            
            logger.info("Milestone approved: id={}, campaignId={}", milestoneId, milestone.getCampaignId());
            
        } catch (SQLException e) {
            logger.error("Database error while approving milestone", e);
            throw new BusinessException("Failed to approve milestone", e);
        }
    }
    
    /**
     * Rejects a milestone (called after voting fails or admin rejection).
     * 
     * @param milestoneId the milestone ID
     * @throws ValidationException if validation fails
     * @throws BusinessException if rejection fails
     */
    public void rejectMilestone(Long milestoneId) throws ValidationException, BusinessException {
        Validator.validatePositive(milestoneId, "Milestone ID");
        
        try {
            Milestone milestone = milestoneRepository.findById(milestoneId);
            
            if (milestone == null) {
                throw new BusinessException("Milestone not found");
            }
            
            if (milestone.getStatus() != MilestoneStatus.UNDER_REVIEW) {
                throw new BusinessException("Can only reject milestones under review");
            }
            
            milestone.setStatus(MilestoneStatus.REJECTED);
            milestoneRepository.update(milestone);
            
            logger.info("Milestone rejected: id={}, campaignId={}", milestoneId, milestone.getCampaignId());
            
        } catch (SQLException e) {
            logger.error("Database error while rejecting milestone", e);
            throw new BusinessException("Failed to reject milestone", e);
        }
    }
    
    /**
     * Updates a milestone's status.
     * 
     * @param milestoneId the milestone ID
     * @param status the new status
     * @throws ValidationException if validation fails
     * @throws BusinessException if update fails
     */
    public void updateMilestoneStatus(Long milestoneId, MilestoneStatus status) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(milestoneId, "Milestone ID");
        Validator.validateNotNull(status, "Milestone status");
        
        try {
            Milestone milestone = milestoneRepository.findById(milestoneId);
            
            if (milestone == null) {
                throw new BusinessException("Milestone not found");
            }
            
            milestone.setStatus(status);
            milestoneRepository.update(milestone);
            
            logger.info("Milestone status updated: id={}, newStatus={}", milestoneId, status);
            
        } catch (SQLException e) {
            logger.error("Database error while updating milestone status", e);
            throw new BusinessException("Failed to update milestone status", e);
        }
    }
}
