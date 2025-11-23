package com.crowdaid.repository.interfaces;

import com.crowdaid.model.reward.Reward;
import com.crowdaid.model.reward.RewardCategory;
import com.crowdaid.model.reward.RewardStatus;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Reward entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface RewardRepository {
    
    /**
     * Finds a reward by ID.
     * 
     * @param id the reward ID
     * @return the reward, or null if not found
     * @throws SQLException if database error occurs
     */
    Reward findById(Long id) throws SQLException;
    
    /**
     * Finds all available rewards.
     * 
     * @return list of available rewards
     * @throws SQLException if database error occurs
     */
    List<Reward> findAllAvailable() throws SQLException;
    
    /**
     * Finds all rewards (including unavailable).
     * 
     * @return list of all rewards
     * @throws SQLException if database error occurs
     */
    List<Reward> findAll() throws SQLException;
    
    /**
     * Finds rewards by category.
     * 
     * @param category the reward category
     * @return list of rewards in the category
     * @throws SQLException if database error occurs
     */
    List<Reward> findByCategory(RewardCategory category) throws SQLException;
    
    /**
     * Finds rewards by status.
     * 
     * @param status the reward status
     * @return list of rewards with the status
     * @throws SQLException if database error occurs
     */
    List<Reward> findByStatus(RewardStatus status) throws SQLException;
    
    /**
     * Saves a new reward.
     * 
     * @param reward the reward to save
     * @return the saved reward with generated ID
     * @throws SQLException if database error occurs
     */
    Reward save(Reward reward) throws SQLException;
    
    /**
     * Updates an existing reward.
     * 
     * @param reward the reward to update
     * @throws SQLException if database error occurs
     */
    void update(Reward reward) throws SQLException;
    
    /**
     * Deletes a reward.
     * 
     * @param id the reward ID
     * @throws SQLException if database error occurs
     */
    void delete(Long id) throws SQLException;
    
    /**
     * Decrements reward stock by one.
     * 
     * @param rewardId the reward ID
     * @return true if successful, false if out of stock
     * @throws SQLException if database error occurs
     */
    boolean decrementStock(Long rewardId) throws SQLException;
}
