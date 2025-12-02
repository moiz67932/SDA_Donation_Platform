package com.crowdaid.repository.interfaces;

import com.crowdaid.model.campaign.Evidence;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Evidence entity.
 * Defines CRUD operations for milestone evidence.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface EvidenceRepository {
    
    /**
     * Saves evidence to the database.
     * 
     * @param evidence the evidence to save
     * @return the saved evidence with generated ID
     * @throws SQLException if database error occurs
     */
    Evidence save(Evidence evidence) throws SQLException;
    
    /**
     * Finds evidence by ID.
     * 
     * @param id the evidence ID
     * @return the evidence, or null if not found
     * @throws SQLException if database error occurs
     */
    Evidence findById(Long id) throws SQLException;
    
    /**
     * Finds all evidence for a milestone.
     * 
     * @param milestoneId the milestone ID
     * @return list of evidence
     * @throws SQLException if database error occurs
     */
    List<Evidence> findByMilestone(Long milestoneId) throws SQLException;
    
    /**
     * Deletes evidence by ID.
     * 
     * @param id the evidence ID
     * @throws SQLException if database error occurs
     */
    void delete(Long id) throws SQLException;
    
    /**
     * Deletes all evidence for a milestone.
     * 
     * @param milestoneId the milestone ID
     * @throws SQLException if database error occurs
     */
    void deleteByMilestone(Long milestoneId) throws SQLException;
}
