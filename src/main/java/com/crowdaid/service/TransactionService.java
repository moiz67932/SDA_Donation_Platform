package com.crowdaid.service;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.model.donation.Transaction;
import com.crowdaid.model.donation.TransactionStatus;
import com.crowdaid.model.donation.TransactionType;
import com.crowdaid.repository.interfaces.TransactionRepository;
import com.crowdaid.repository.mysql.MySQLTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * TransactionService handles transaction logging and tracking.
 * Records all financial transactions in the system.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;
    
    /**
     * Constructor initializing the transaction repository.
     */
    public TransactionService() {
        this.transactionRepository = new MySQLTransactionRepository();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param transactionRepository the transaction repository
     */
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    /**
     * Records a new transaction.
     * 
     * @param donorId the donor's user ID (null for system transactions)
     * @param gatewayId the payment gateway ID
     * @param amount the transaction amount
     * @param type the transaction type
     * @param status the transaction status
     * @param paymentMethod the payment method used
     * @param reference additional reference information
     * @return the created transaction
     * @throws BusinessException if operation fails
     */
    public Transaction recordTransaction(Long donorId, Long gatewayId, double amount,
                                        TransactionType type, TransactionStatus status,
                                        String paymentMethod, String reference) throws BusinessException {
        try {
            Transaction transaction = new Transaction();
            transaction.setDonorId(donorId);
            transaction.setGatewayId(gatewayId);
            transaction.setAmount(amount);
            transaction.setType(type);
            transaction.setStatus(status);
            transaction.setPaymentMethod(paymentMethod);
            transaction.setReference(reference);
            transaction.setTransactionDate(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
            
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            logger.info("Transaction recorded: id={}, type={}, amount={}, status={}", 
                       savedTransaction.getId(), type, amount, status);
            
            return savedTransaction;
        } catch (SQLException e) {
            logger.error("Error recording transaction: type={}, amount={}", type, amount, e);
            throw new BusinessException("Failed to record transaction", e);
        }
    }
    
    /**
     * Logs a transaction with all details.
     * 
     * @param campaignId the campaign ID
     * @param donorId the donor ID (can be null for some transaction types)
     * @param type the transaction type
     * @param amount the transaction amount
     * @param status the transaction status
     * @param description the description
     * @param reference the reference string
     * @return the saved transaction
     * @throws BusinessException if operation fails
     */
    public Transaction logTransaction(Long campaignId, Long donorId, Object type, double amount, 
                                     Object status, String description, String reference) 
            throws BusinessException {
        try {
            Transaction transaction = new Transaction();
            transaction.setCampaignId(campaignId);
            transaction.setDonorId(donorId);
            transaction.setAmount(amount);
            
            // Set type if provided
            if (type instanceof TransactionType) {
                transaction.setType((TransactionType) type);
            }
            
            // Set status if provided
            if (status instanceof TransactionStatus) {
                transaction.setStatus((TransactionStatus) status);
            }
            
            transaction.setReference(reference);
            transaction.setDescription(description);
            
            return transactionRepository.save(transaction);
        } catch (SQLException e) {
            logger.error("Error logging transaction", e);
            throw new BusinessException("Failed to log transaction", e);
        }
    }
    
    /**
     * Gets all transactions for a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of transactions
     * @throws BusinessException if operation fails
     */
    public List<Transaction> getDonorTransactions(Long donorId) throws BusinessException {
        try {
            return transactionRepository.findByDonor(donorId);
        } catch (SQLException e) {
            logger.error("Error retrieving donor transactions: donorId={}", donorId, e);
            throw new BusinessException("Failed to retrieve transactions", e);
        }
    }
    
    /**
     * Gets a transaction by ID.
     * 
     * @param transactionId the transaction ID
     * @return the transaction, or null if not found
     * @throws BusinessException if operation fails
     */
    public Transaction getTransaction(Long transactionId) throws BusinessException {
        try {
            return transactionRepository.findById(transactionId);
        } catch (SQLException e) {
            logger.error("Error retrieving transaction: transactionId={}", transactionId, e);
            throw new BusinessException("Failed to retrieve transaction", e);
        }
    }
}
