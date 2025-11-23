package com.crowdaid.repository.interfaces;

import com.crowdaid.model.donation.Transaction;
import com.crowdaid.model.donation.TransactionType;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Transaction entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface TransactionRepository {
    
    /**
     * Finds a transaction by ID.
     * 
     * @param id the transaction ID
     * @return the transaction, or null if not found
     * @throws SQLException if database error occurs
     */
    Transaction findById(Long id) throws SQLException;
    
    /**
     * Finds all transactions for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return list of transactions
     * @throws SQLException if database error occurs
     */
    List<Transaction> findByCampaign(Long campaignId) throws SQLException;
    
    /**
     * Finds all transactions by a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of transactions
     * @throws SQLException if database error occurs
     */
    List<Transaction> findByDonor(Long donorId) throws SQLException;
    
    /**
     * Finds transactions by type.
     * 
     * @param type the transaction type
     * @return list of transactions
     * @throws SQLException if database error occurs
     */
    List<Transaction> findByType(TransactionType type) throws SQLException;
    
    /**
     * Saves a new transaction.
     * 
     * @param transaction the transaction to save
     * @return the saved transaction with generated ID
     * @throws SQLException if database error occurs
     */
    Transaction save(Transaction transaction) throws SQLException;
    
    /**
     * Updates an existing transaction.
     * 
     * @param transaction the transaction to update
     * @throws SQLException if database error occurs
     */
    void update(Transaction transaction) throws SQLException;
}
