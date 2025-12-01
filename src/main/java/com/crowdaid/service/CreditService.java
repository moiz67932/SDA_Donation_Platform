package com.crowdaid.service;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.repository.interfaces.CreditRepository;
import com.crowdaid.repository.mysql.MySQLCreditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * CreditService handles credit management operations.
 * Manages earning and spending of virtual credits for donors.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class CreditService {
    
    private static final Logger logger = LoggerFactory.getLogger(CreditService.class);
    private final CreditRepository creditRepository;
    
    // Credit award rate: 1 credit per 100 currency units
    private static final double CREDIT_RATE = 100.0;
    
    /**
     * Constructor initializing the credit repository.
     */
    public CreditService() {
        this.creditRepository = new MySQLCreditRepository();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param creditRepository the credit repository
     */
    public CreditService(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }
    
    /**
     * Awards credits to a donor based on donation amount.
     * Awards 1 credit per 100 currency units donated.
     * 
     * @param donorId the donor's user ID
     * @param donationAmount the amount donated
     * @return the number of credits awarded
     * @throws BusinessException if operation fails
     */
    public int awardCreditsForDonation(Long donorId, double donationAmount) throws BusinessException {
        try {
            int creditsToAward = (int) Math.floor(donationAmount / CREDIT_RATE);
            
            if (creditsToAward > 0) {
                creditRepository.addCredits(donorId, creditsToAward);
                logger.info("Awarded {} credits to donor {} for donation of {}", 
                           creditsToAward, donorId, donationAmount);
            }
            
            return creditsToAward;
        } catch (SQLException e) {
            logger.error("Error awarding credits to donor: donorId={}", donorId, e);
            throw new BusinessException("Failed to award credits", e);
        }
    }
    
    /**
     * Adds credits to a donor with source tracking.
     * 
     * @param donorId the donor's user ID
     * @param amount the donation amount
     * @param source the source of credits
     * @return the number of credits awarded
     * @throws BusinessException if operation fails
     */
    public int addCredits(Long donorId, double amount, String source) throws BusinessException {
        return awardCreditsForDonation(donorId, amount);
    }
    
    /**
     * Deducts credits from a donor's balance.
     * 
     * @param donorId the donor's user ID
     * @param creditAmount the amount of credits to deduct
     * @return true if successful, false if insufficient balance
     * @throws BusinessException if operation fails
     */
    public boolean deductCredits(Long donorId, int creditAmount) throws BusinessException {
        try {
            boolean success = creditRepository.deductCredits(donorId, creditAmount);
            
            if (success) {
                logger.info("Deducted {} credits from donor {}", creditAmount, donorId);
            } else {
                logger.warn("Insufficient credits for donor {}: attempted to deduct {}", 
                           donorId, creditAmount);
            }
            
            return success;
        } catch (SQLException e) {
            logger.error("Error deducting credits from donor: donorId={}", donorId, e);
            throw new BusinessException("Failed to deduct credits", e);
        }
    }
    
    /**
     * Deducts credits from a donor's balance with source tracking.
     * 
     * @param donorId the donor's user ID
     * @param creditAmount the amount of credits to deduct
     * @param source the source/reason for deduction
     * @return true if successful, false if insufficient balance
     * @throws BusinessException if operation fails
     */
    public boolean deductCredits(Long donorId, double creditAmount, String source) throws BusinessException {
        return deductCredits(donorId, (int) creditAmount);
    }
    
    /**
     * Gets the credit balance for a donor.
     * 
     * @param donorId the donor's user ID
     * @return the credit balance
     * @throws BusinessException if operation fails
     */
    public int getCreditBalance(Long donorId) throws BusinessException {
        try {
            double balance = creditRepository.getBalance(donorId);
            return (int) balance;
        } catch (SQLException e) {
            logger.error("Error retrieving credit balance for donor: donorId={}", donorId, e);
            throw new BusinessException("Failed to retrieve credit balance", e);
        }
    }
    
    /**
     * Checks if a donor has sufficient credits.
     * 
     * @param donorId the donor's user ID
     * @param requiredCredits the required credit amount
     * @return true if donor has sufficient credits
     * @throws BusinessException if operation fails
     */
    public boolean hasSufficientCredits(Long donorId, int requiredCredits) throws BusinessException {
        int balance = getCreditBalance(donorId);
        return balance >= requiredCredits;
    }
}
