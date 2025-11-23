package com.crowdaid.repository.interfaces;

import com.crowdaid.model.reward.Redemption;
import com.crowdaid.model.reward.RedemptionStatus;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Redemption entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface RedemptionRepository {
    
    /**
     * Finds a redemption by ID.
     * 
     * @param id the redemption ID
     * @return the redemption, or null if not found
     * @throws SQLException if database error occurs
     */
    Redemption findById(Long id) throws SQLException;
    
    /**
     * Finds all redemptions by a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of redemptions
     * @throws SQLException if database error occurs
     */
    List<Redemption> findByDonor(Long donorId) throws SQLException;
    
    /**
     * Finds all redemptions for a reward.
     * 
     * @param rewardId the reward ID
     * @return list of redemptions
     * @throws SQLException if database error occurs
     */
    List<Redemption> findByReward(Long rewardId) throws SQLException;
    
    /**
     * Finds redemptions by status.
     * 
     * @param status the redemption status
     * @return list of redemptions with the status
     * @throws SQLException if database error occurs
     */
    List<Redemption> findByStatus(RedemptionStatus status) throws SQLException;
    
    /**
     * Saves a new redemption.
     * 
     * @param redemption the redemption to save
     * @return the saved redemption with generated ID
     * @throws SQLException if database error occurs
     */
    Redemption save(Redemption redemption) throws SQLException;
    
    /**
     * Updates an existing redemption.
     * 
     * @param redemption the redemption to update
     * @throws SQLException if database error occurs
     */
    void update(Redemption redemption) throws SQLException;
}
