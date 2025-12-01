package com.crowdaid.repository.interfaces;

import com.crowdaid.model.donation.SubscriptionTier;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for SubscriptionTier entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface SubscriptionTierRepository {
    
    /**
     * Finds a subscription tier by ID.
     * 
     * @param id the tier ID
     * @return the subscription tier, or null if not found
     * @throws SQLException if database error occurs
     */
    SubscriptionTier findById(Long id) throws SQLException;
    
    /**
     * Finds all subscription tiers for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return list of subscription tiers
     * @throws SQLException if database error occurs
     */
    List<SubscriptionTier> findByCampaign(Long campaignId) throws SQLException;
    
    /**
     * Finds a subscription tier by campaign ID and tier name.
     * 
     * @param campaignId the campaign ID
     * @param tierName the tier name
     * @return the subscription tier, or null if not found
     * @throws SQLException if database error occurs
     */
    SubscriptionTier findByCampaignAndName(Long campaignId, String tierName) throws SQLException;
    
    /**
     * Saves a new subscription tier.
     * 
     * @param tier the subscription tier to save
     * @return the saved tier with generated ID
     * @throws SQLException if database error occurs
     */
    SubscriptionTier save(SubscriptionTier tier) throws SQLException;
    
    /**
     * Updates an existing subscription tier.
     * 
     * @param tier the subscription tier to update
     * @throws SQLException if database error occurs
     */
    void update(SubscriptionTier tier) throws SQLException;
    
    /**
     * Deletes a subscription tier.
     * 
     * @param id the tier ID to delete
     * @throws SQLException if database error occurs
     */
    void delete(Long id) throws SQLException;
    
    /**
     * Counts the number of active subscriptions for a tier.
     * 
     * @param tierId the tier ID
     * @return count of active subscriptions
     * @throws SQLException if database error occurs
     */
    int countActiveSubscriptions(Long tierId) throws SQLException;
}
