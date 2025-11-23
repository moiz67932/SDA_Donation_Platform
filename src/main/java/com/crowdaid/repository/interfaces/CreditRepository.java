package com.crowdaid.repository.interfaces;

import com.crowdaid.model.user.Credit;

import java.sql.SQLException;

/**
 * Repository interface for Credit entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface CreditRepository {
    
    /**
     * Finds a credit record by donor ID.
     * 
     * @param donorId the donor's user ID
     * @return the credit record, or null if not found
     * @throws SQLException if database error occurs
     */
    Credit findByDonor(Long donorId) throws SQLException;
    
    /**
     * Saves a new credit record.
     * 
     * @param credit the credit to save
     * @return the saved credit with generated ID
     * @throws SQLException if database error occurs
     */
    Credit save(Credit credit) throws SQLException;
    
    /**
     * Updates an existing credit record.
     * 
     * @param credit the credit to update
     * @throws SQLException if database error occurs
     */
    void update(Credit credit) throws SQLException;
    
    /**
     * Adds credits to a donor's balance.
     * 
     * @param donorId the donor's user ID
     * @param amount the amount to add
     * @throws SQLException if database error occurs
     */
    void addCredits(Long donorId, double amount) throws SQLException;
    
    /**
     * Deducts credits from a donor's balance.
     * 
     * @param donorId the donor's user ID
     * @param amount the amount to deduct
     * @return true if successful, false if insufficient balance
     * @throws SQLException if database error occurs
     */
    boolean deductCredits(Long donorId, double amount) throws SQLException;
    
    /**
     * Gets the credit balance for a donor.
     * 
     * @param donorId the donor's user ID
     * @return the credit balance
     * @throws SQLException if database error occurs
     */
    double getBalance(Long donorId) throws SQLException;
}
