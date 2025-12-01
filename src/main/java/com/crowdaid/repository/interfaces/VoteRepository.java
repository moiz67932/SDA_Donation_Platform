package com.crowdaid.repository.interfaces;

import com.crowdaid.model.voting.Vote;
import com.crowdaid.model.voting.VoteType;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Repository interface for Vote entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface VoteRepository {
    
    /**
     * Finds a vote by ID.
     * 
     * @param id the vote ID
     * @return the vote, or null if not found
     * @throws SQLException if database error occurs
     */
    Vote findById(Long id) throws SQLException;
    
    /**
     * Finds all votes for a milestone.
     * 
     * @param milestoneId the milestone ID
     * @return list of votes
     * @throws SQLException if database error occurs
     */
    List<Vote> findByMilestone(Long milestoneId) throws SQLException;
    
    /**
     * Finds votes by a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of votes
     * @throws SQLException if database error occurs
     */
    List<Vote> findByDonor(Long donorId) throws SQLException;
    
    /**
     * Checks if a donor has already voted on a milestone.
     * 
     * @param donorId the donor's user ID
     * @param milestoneId the milestone ID
     * @return true if vote exists
     * @throws SQLException if database error occurs
     */
    boolean hasVoted(Long donorId, Long milestoneId) throws SQLException;
    
    /**
     * Saves a new vote.
     * 
     * @param vote the vote to save
     * @return the saved vote with generated ID
     * @throws SQLException if database error occurs
     */
    Vote save(Vote vote) throws SQLException;
    
    /**
     * Gets vote tally for a milestone.
     * 
     * @param milestoneId the milestone ID
     * @return map of vote types to weighted vote counts
     * @throws SQLException if database error occurs
     */
    Map<VoteType, Double> getVoteTally(Long milestoneId) throws SQLException;
    
    /**
     * Calculates approval percentage for a milestone.
     * 
     * @param milestoneId the milestone ID
     * @return approval percentage (0-100)
     * @throws SQLException if database error occurs
     */
    double getApprovalPercentage(Long milestoneId) throws SQLException;
    
    /**
     * Gets vote statistics for a milestone.
     * 
     * @param milestoneId the milestone ID
     * @return map containing vote statistics
     * @throws SQLException if database error occurs
     */
    Map<String, Object> getVoteStatistics(Long milestoneId) throws SQLException;
}
