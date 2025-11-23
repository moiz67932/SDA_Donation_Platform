package com.crowdaid.repository.interfaces;

import com.crowdaid.model.campaign.Milestone;
import com.crowdaid.model.campaign.MilestoneStatus;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Milestone entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface MilestoneRepository {
    
    /**
     * Finds a milestone by ID.
     * 
     * @param id the milestone ID
     * @return the milestone, or null if not found
     * @throws SQLException if database error occurs
     */
    Milestone findById(Long id) throws SQLException;
    
    /**
     * Finds all milestones for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return list of milestones
     * @throws SQLException if database error occurs
     */
    List<Milestone> findByCampaign(Long campaignId) throws SQLException;
    
    /**
     * Finds milestones by status.
     * 
     * @param status the milestone status
     * @return list of milestones with the status
     * @throws SQLException if database error occurs
     */
    List<Milestone> findByStatus(MilestoneStatus status) throws SQLException;
    
    /**
     * Finds milestones under review that require voting.
     * 
     * @return list of milestones under review
     * @throws SQLException if database error occurs
     */
    List<Milestone> findUnderReview() throws SQLException;
    
    /**
     * Saves a new milestone.
     * 
     * @param milestone the milestone to save
     * @return the saved milestone with generated ID
     * @throws SQLException if database error occurs
     */
    Milestone save(Milestone milestone) throws SQLException;
    
    /**
     * Updates an existing milestone.
     * 
     * @param milestone the milestone to update
     * @throws SQLException if database error occurs
     */
    void update(Milestone milestone) throws SQLException;
    
    /**
     * Updates milestone status.
     * 
     * @param milestoneId the milestone ID
     * @param newStatus the new status
     * @throws SQLException if database error occurs
     */
    void updateStatus(Long milestoneId, MilestoneStatus newStatus) throws SQLException;
    
    /**
     * Deletes a milestone.
     * 
     * @param id the milestone ID
     * @throws SQLException if database error occurs
     */
    void delete(Long id) throws SQLException;
}
