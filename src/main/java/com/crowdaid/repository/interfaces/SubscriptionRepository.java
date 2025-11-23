package com.crowdaid.repository.interfaces;

import com.crowdaid.model.donation.Subscription;
import com.crowdaid.model.donation.SubscriptionStatus;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Subscription entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface SubscriptionRepository {
    
    /**
     * Finds a subscription by ID.
     * 
     * @param id the subscription ID
     * @return the subscription, or null if not found
     * @throws SQLException if database error occurs
     */
    Subscription findById(Long id) throws SQLException;
    
    /**
     * Finds all subscriptions for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return list of subscriptions
     * @throws SQLException if database error occurs
     */
    List<Subscription> findByCampaign(Long campaignId) throws SQLException;
    
    /**
     * Finds all subscriptions by a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of subscriptions
     * @throws SQLException if database error occurs
     */
    List<Subscription> findByDonor(Long donorId) throws SQLException;
    
    /**
     * Finds active subscriptions for a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of active subscriptions
     * @throws SQLException if database error occurs
     */
    List<Subscription> findActiveByDonor(Long donorId) throws SQLException;
    
    /**
     * Checks if a donor has an active subscription to a campaign.
     * 
     * @param donorId the donor's user ID
     * @param campaignId the campaign ID
     * @return true if active subscription exists
     * @throws SQLException if database error occurs
     */
    boolean hasActiveSubscription(Long donorId, Long campaignId) throws SQLException;
    
    /**
     * Saves a new subscription.
     * 
     * @param subscription the subscription to save
     * @return the saved subscription with generated ID
     * @throws SQLException if database error occurs
     */
    Subscription save(Subscription subscription) throws SQLException;
    
    /**
     * Updates an existing subscription.
     * 
     * @param subscription the subscription to update
     * @throws SQLException if database error occurs
     */
    void update(Subscription subscription) throws SQLException;
    
    /**
     * Updates subscription status.
     * 
     * @param subscriptionId the subscription ID
     * @param newStatus the new status
     * @throws SQLException if database error occurs
     */
    void updateStatus(Long subscriptionId, SubscriptionStatus newStatus) throws SQLException;
}
