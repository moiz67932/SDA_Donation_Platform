package com.crowdaid.service;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.CampaignCategory;
import com.crowdaid.model.campaign.CampaignStatus;
import com.crowdaid.model.donation.EscrowAccount;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.interfaces.EscrowRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.repository.mysql.MySQLEscrowRepository;
import com.crowdaid.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * CampaignService handles campaign management operations.
 * 
 * Implements UC3 (Create Campaign), UC6 (Browse Campaigns), and UC11 (Approve Campaign).
 * 
 * Applies GRASP principles:
 * - Controller: Coordinates campaign creation workflow
 * - Information Expert: Campaign-related business logic
 * - Low Coupling: Depends on repository interfaces
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class CampaignService {
    
    private static final Logger logger = LoggerFactory.getLogger(CampaignService.class);
    private final CampaignRepository campaignRepository;
    private final EscrowRepository escrowRepository;
    
    /**
     * Constructor initializing repositories.
     */
    public CampaignService() {
        this.campaignRepository = new MySQLCampaignRepository();
        this.escrowRepository = new MySQLEscrowRepository();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param campaignRepository the campaign repository
     * @param escrowRepository the escrow repository
     */
    public CampaignService(CampaignRepository campaignRepository, EscrowRepository escrowRepository) {
        this.campaignRepository = campaignRepository;
        this.escrowRepository = escrowRepository;
    }
    
    /**
     * Creates a new campaign (UC3: Create Campaign).
     * 
     * @param campaignerId the campaigner's user ID
     * @param title the campaign title
     * @param description the campaign description
     * @param goalAmount the fundraising goal
     * @param category the campaign category
     * @param startDate the start date
     * @param endDate the end date
     * @param philanthropic whether campaign is philanthropic
     * @param civic whether campaign is civic
     * @param imageUrl the campaign image URL (optional)
     * @return the created campaign
     * @throws ValidationException if validation fails
     * @throws BusinessException if creation fails
     */
    public Campaign createCampaign(Long campaignerId, String title, String description,
                                  double goalAmount, CampaignCategory category,
                                  LocalDate startDate, LocalDate endDate,
                                  boolean philanthropic, boolean civic, String imageUrl)
            throws ValidationException, BusinessException {
        
        // Validate inputs
        Validator.validatePositive(campaignerId, "Campaigner ID");
        Validator.validateNonEmpty(title, "Campaign title");
        Validator.validateNonEmpty(description, "Campaign description");
        Validator.validatePositive(goalAmount, "Goal amount");
        Validator.validateNotNull(category, "Campaign category");
        Validator.validateNotNull(startDate, "Start date");
        Validator.validateNotNull(endDate, "End date");
        
        if (endDate.isBefore(startDate)) {
            throw new ValidationException("End date must be after start date");
        }
        
        if (startDate.isBefore(LocalDate.now())) {
            throw new ValidationException("Start date cannot be in the past");
        }
        
        try {
            // Create campaign entity
            Campaign campaign = new Campaign(campaignerId, title, description, goalAmount, category);
            campaign.setStartDate(startDate);
            campaign.setEndDate(endDate);
            campaign.setPhilanthropic(philanthropic);
            campaign.setCivic(civic);
            campaign.setImageUrl(imageUrl);
            campaign.setStatus(CampaignStatus.PENDING_REVIEW);
            
            // Save campaign
            Campaign savedCampaign = campaignRepository.save(campaign);
            
            // Create escrow account for the campaign
            EscrowAccount escrowAccount = new EscrowAccount(savedCampaign.getId());
            escrowRepository.save(escrowAccount);
            
            logger.info("Campaign created successfully: id={}, title={}, campaignerId={}", 
                       savedCampaign.getId(), title, campaignerId);
            
            return savedCampaign;
            
        } catch (SQLException e) {
            logger.error("Database error while creating campaign", e);
            throw new BusinessException("Failed to create campaign", e);
        }
    }
    
    /**
     * Retrieves a campaign by ID.
     * 
     * @param campaignId the campaign ID
     * @return the campaign
     * @throws ValidationException if validation fails
     * @throws BusinessException if campaign not found
     */
    public Campaign getCampaignById(Long campaignId) throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        
        try {
            Campaign campaign = campaignRepository.findById(campaignId);
            
            if (campaign == null) {
                logger.warn("Campaign not found: id={}", campaignId);
                throw new BusinessException("Campaign not found with ID: " + campaignId);
            }
            
            logger.debug("Campaign retrieved: id={}, title={}", campaign.getId(), campaign.getTitle());
            return campaign;
            
        } catch (SQLException e) {
            logger.error("Database error while finding campaign", e);
            throw new BusinessException("Failed to retrieve campaign", e);
        }
    }
    
    /**
     * Browses all active campaigns (UC6: Browse Campaigns).
     * 
     * @return list of active campaigns
     * @throws BusinessException if retrieval fails
     */
    public List<Campaign> browseActiveCampaigns() throws BusinessException {
        try {
            List<Campaign> campaigns = campaignRepository.findAllActive();
            logger.debug("Retrieved {} active campaigns", campaigns.size());
            return campaigns;
            
        } catch (SQLException e) {
            logger.error("Database error while browsing campaigns", e);
            throw new BusinessException("Failed to retrieve campaigns", e);
        }
    }
    
    /**
     * Retrieves campaigns by category.
     * 
     * @param category the campaign category
     * @return list of campaigns in the category
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Campaign> getCampaignsByCategory(CampaignCategory category) 
            throws ValidationException, BusinessException {
        Validator.validateNotNull(category, "Campaign category");
        
        try {
            List<Campaign> campaigns = campaignRepository.findByCategory(category);
            logger.debug("Retrieved {} campaigns for category {}", campaigns.size(), category);
            return campaigns;
            
        } catch (SQLException e) {
            logger.error("Database error while finding campaigns by category", e);
            throw new BusinessException("Failed to retrieve campaigns", e);
        }
    }
    
    /**
     * Retrieves campaigns by campaigner.
     * 
     * @param campaignerId the campaigner's user ID
     * @return list of campaigns
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Campaign> getCampaignsByCampaigner(Long campaignerId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignerId, "Campaigner ID");
        
        try {
            List<Campaign> campaigns = campaignRepository.findByCampaigner(campaignerId);
            logger.debug("Retrieved {} campaigns for campaigner {}", campaigns.size(), campaignerId);
            return campaigns;
            
        } catch (SQLException e) {
            logger.error("Database error while finding campaigns by campaigner", e);
            throw new BusinessException("Failed to retrieve campaigns", e);
        }
    }
    
    /**
     * Retrieves campaigns by status (admin operation).
     * 
     * @param status the campaign status
     * @return list of campaigns with the status
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Campaign> getCampaignsByStatus(CampaignStatus status) 
            throws ValidationException, BusinessException {
        Validator.validateNotNull(status, "Campaign status");
        
        try {
            List<Campaign> campaigns = campaignRepository.findByStatus(status);
            logger.debug("Retrieved {} campaigns with status {}", campaigns.size(), status);
            return campaigns;
            
        } catch (SQLException e) {
            logger.error("Database error while finding campaigns by status", e);
            throw new BusinessException("Failed to retrieve campaigns", e);
        }
    }
    
    /**
     * Approves a campaign (UC11: Approve Campaign - Admin).
     * 
     * @param campaignId the campaign ID
     * @param adminId the admin's user ID
     * @throws ValidationException if validation fails
     * @throws BusinessException if approval fails
     */
    public void approveCampaign(Long campaignId, Long adminId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        Validator.validatePositive(adminId, "Admin ID");
        
        try {
            Campaign campaign = campaignRepository.findById(campaignId);
            
            if (campaign == null) {
                throw new BusinessException("Campaign not found");
            }
            
            if (campaign.getStatus() != CampaignStatus.PENDING_REVIEW) {
                throw new BusinessException("Campaign is not pending review. Current status: " + 
                                          campaign.getStatus());
            }
            
            campaign.setStatus(CampaignStatus.ACTIVE);
            campaignRepository.update(campaign);
            
            logger.info("Campaign approved: id={}, title={}, adminId={}", 
                       campaignId, campaign.getTitle(), adminId);
            
        } catch (SQLException e) {
            logger.error("Database error while approving campaign", e);
            throw new BusinessException("Failed to approve campaign", e);
        }
    }
    
    /**
     * Rejects a campaign (UC11: Approve Campaign - Admin).
     * 
     * @param campaignId the campaign ID
     * @param adminId the admin's user ID
     * @param reason the rejection reason
     * @throws ValidationException if validation fails
     * @throws BusinessException if rejection fails
     */
    public void rejectCampaign(Long campaignId, Long adminId, String reason) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        Validator.validatePositive(adminId, "Admin ID");
        Validator.validateNonEmpty(reason, "Rejection reason");
        
        try {
            Campaign campaign = campaignRepository.findById(campaignId);
            
            if (campaign == null) {
                throw new BusinessException("Campaign not found");
            }
            
            if (campaign.getStatus() != CampaignStatus.PENDING_REVIEW) {
                throw new BusinessException("Campaign is not pending review");
            }
            
            campaign.setStatus(CampaignStatus.REJECTED);
            campaignRepository.update(campaign);
            
            logger.info("Campaign rejected: id={}, title={}, adminId={}, reason={}", 
                       campaignId, campaign.getTitle(), adminId, reason);
            
        } catch (SQLException e) {
            logger.error("Database error while rejecting campaign", e);
            throw new BusinessException("Failed to reject campaign", e);
        }
    }
    
    /**
     * Updates a campaign's collected amount.
     * 
     * @param campaignId the campaign ID
     * @param amount the amount to add
     * @throws ValidationException if validation fails
     * @throws BusinessException if update fails
     */
    public void updateCollectedAmount(Long campaignId, double amount) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        Validator.validatePositive(amount, "Amount");
        
        try {
            Campaign campaign = campaignRepository.findById(campaignId);
            
            if (campaign == null) {
                throw new BusinessException("Campaign not found");
            }
            
            campaign.setCollectedAmount(campaign.getCollectedAmount() + amount);
            campaignRepository.update(campaign);
            
            logger.debug("Campaign collected amount updated: id={}, newTotal={}", 
                        campaignId, campaign.getCollectedAmount());
            
        } catch (SQLException e) {
            logger.error("Database error while updating collected amount", e);
            throw new BusinessException("Failed to update collected amount", e);
        }
    }
    
    /**
     * Gets campaigns by campaigner.
     * 
     * @param campaignerId the campaigner's user ID
     * @return list of campaigns
     * @throws BusinessException if operation fails
     */
    public List<Campaign> getCampaignerCampaigns(Long campaignerId) throws BusinessException {
        try {
            return campaignRepository.findByCampaigner(campaignerId);
        } catch (SQLException e) {
            logger.error("Error retrieving campaigns for campaigner: campaignerId={}", campaignerId, e);
            throw new BusinessException("Failed to retrieve campaigns", e);
        }
    }
    
    /**
     * Searches for campaigns by keyword.
     * 
     * @param keyword the search keyword
     * @return list of matching campaigns
     * @throws ValidationException if validation fails
     * @throws BusinessException if search fails
     */
    public List<Campaign> searchCampaigns(String keyword) 
            throws ValidationException, BusinessException {
        Validator.validateNonEmpty(keyword, "Search keyword");
        
        try {
            List<Campaign> campaigns = campaignRepository.searchByKeyword(keyword);
            logger.debug("Found {} campaigns matching keyword: {}", campaigns.size(), keyword);
            return campaigns;
            
        } catch (SQLException e) {
            logger.error("Database error while searching campaigns", e);
            throw new BusinessException("Failed to search campaigns", e);
        }
    }
}
