package com.crowdaid.repository.interfaces;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.CampaignCategory;
import com.crowdaid.model.campaign.CampaignStatus;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Campaign entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface CampaignRepository {
    
    /**
     * Finds a campaign by ID.
     * 
     * @param id the campaign ID
     * @return the campaign, or null if not found
     * @throws SQLException if database error occurs
     */
    Campaign findById(Long id) throws SQLException;
    
    /**
     * Finds all active campaigns.
     * 
     * @return list of active campaigns
     * @throws SQLException if database error occurs
     */
    List<Campaign> findAllActive() throws SQLException;
    
    /**
     * Finds campaigns by campaigner ID.
     * 
     * @param campaignerId the campaigner's user ID
     * @return list of campaigns
     * @throws SQLException if database error occurs
     */
    List<Campaign> findByCampaigner(Long campaignerId) throws SQLException;
    
    /**
     * Finds campaigns by status.
     * 
     * @param status the campaign status
     * @return list of campaigns with the status
     * @throws SQLException if database error occurs
     */
    List<Campaign> findByStatus(CampaignStatus status) throws SQLException;
    
    /**
     * Searches campaigns by keyword and optional category.
     * 
     * @param keyword the search keyword
     * @param category the category filter (null for all)
     * @return list of matching campaigns
     * @throws SQLException if database error occurs
     */
    List<Campaign> search(String keyword, CampaignCategory category) throws SQLException;
    
    /**
     * Finds philanthropic or civic campaigns that earn credits.
     * 
     * @return list of campaigns that earn credits
     * @throws SQLException if database error occurs
     */
    List<Campaign> findCreditEarningCampaigns() throws SQLException;
    
    /**
     * Saves a new campaign.
     * 
     * @param campaign the campaign to save
     * @return the saved campaign with generated ID
     * @throws SQLException if database error occurs
     */
    Campaign save(Campaign campaign) throws SQLException;
    
    /**
     * Updates an existing campaign.
     * 
     * @param campaign the campaign to update
     * @throws SQLException if database error occurs
     */
    void update(Campaign campaign) throws SQLException;
    
    /**
     * Updates campaign status.
     * 
     * @param campaignId the campaign ID
     * @param newStatus the new status
     * @throws SQLException if database error occurs
     */
    void updateStatus(Long campaignId, CampaignStatus newStatus) throws SQLException;
    
    /**
     * Updates campaign collected amount.
     * 
     * @param campaignId the campaign ID
     * @param newAmount the new collected amount
     * @throws SQLException if database error occurs
     */
    void updateCollectedAmount(Long campaignId, double newAmount) throws SQLException;
}
