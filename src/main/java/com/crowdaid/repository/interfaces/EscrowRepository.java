package com.crowdaid.repository.interfaces;

import com.crowdaid.model.donation.EscrowAccount;

import java.sql.SQLException;

/**
 * Repository interface for EscrowAccount entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface EscrowRepository {
    
    /**
     * Finds an escrow account by ID.
     * 
     * @param id the escrow account ID
     * @return the escrow account, or null if not found
     * @throws SQLException if database error occurs
     */
    EscrowAccount findById(Long id) throws SQLException;
    
    /**
     * Finds an escrow account by campaign ID.
     * 
     * @param campaignId the campaign ID
     * @return the escrow account, or null if not found
     * @throws SQLException if database error occurs
     */
    EscrowAccount findByCampaign(Long campaignId) throws SQLException;
    
    /**
     * Saves a new escrow account.
     * 
     * @param escrowAccount the escrow account to save
     * @return the saved escrow account with generated ID
     * @throws SQLException if database error occurs
     */
    EscrowAccount save(EscrowAccount escrowAccount) throws SQLException;
    
    /**
     * Updates an existing escrow account.
     * 
     * @param escrowAccount the escrow account to update
     * @throws SQLException if database error occurs
     */
    void update(EscrowAccount escrowAccount) throws SQLException;
    
    /**
     * Adds funds to an escrow account.
     * 
     * @param escrowId the escrow account ID
     * @param amount the amount to add
     * @throws SQLException if database error occurs
     */
    void addFunds(Long escrowId, double amount) throws SQLException;
    
    /**
     * Releases funds from an escrow account.
     * 
     * @param escrowId the escrow account ID
     * @param amount the amount to release
     * @return true if successful, false if insufficient balance
     * @throws SQLException if database error occurs
     */
    boolean releaseFunds(Long escrowId, double amount) throws SQLException;
}
