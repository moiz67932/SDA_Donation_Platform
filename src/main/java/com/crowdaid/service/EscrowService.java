package com.crowdaid.service;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.donation.EscrowAccount;
import com.crowdaid.repository.interfaces.EscrowRepository;
import com.crowdaid.repository.mysql.MySQLEscrowRepository;
import com.crowdaid.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * EscrowService handles escrow fund management operations.
 * 
 * Manages funds held in escrow for campaigns and releases them when milestones are approved.
 * 
 * Applies GRASP principles:
 * - Information Expert: Escrow-related business logic
 * - Low Coupling: Depends on repository interfaces
 * 
 * Business Rules:
 * - Every donation/subscription adds to escrow totalAmount and availableAmount
 * - When milestone approved, releaseFunds decreases availableAmount and increases releasedAmount
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class EscrowService {
    
    private static final Logger logger = LoggerFactory.getLogger(EscrowService.class);
    private final EscrowRepository escrowRepository;
    
    @SuppressWarnings("unused")
    private final NotificationService notificationService;
    
    /**
     * Constructor initializing repositories and services.
     */
    public EscrowService() {
        this.escrowRepository = new MySQLEscrowRepository();
        this.notificationService = new NotificationService();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param escrowRepository the escrow repository
     * @param notificationService the notification service
     */
    public EscrowService(EscrowRepository escrowRepository, NotificationService notificationService) {
        this.escrowRepository = escrowRepository;
        this.notificationService = notificationService;
    }
    
    /**
     * Retrieves escrow account by campaign ID.
     * 
     * @param campaignId the campaign ID
     * @return the escrow account
     * @throws ValidationException if validation fails
     * @throws BusinessException if escrow account not found
     */
    public EscrowAccount getEscrowByCampaign(Long campaignId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        
        try {
            EscrowAccount escrow = escrowRepository.findByCampaign(campaignId);
            
            if (escrow == null) {
                logger.warn("Escrow account not found for campaign: {}", campaignId);
                throw new BusinessException("Escrow account not found for campaign ID: " + campaignId);
            }
            
            logger.debug("Escrow retrieved: campaignId={}, totalAmount={}, availableAmount={}", 
                        campaignId, escrow.getTotalAmount(), escrow.getAvailableAmount());
            return escrow;
            
        } catch (SQLException e) {
            logger.error("Database error while finding escrow account", e);
            throw new BusinessException("Failed to retrieve escrow account", e);
        }
    }
    
    /**
     * Adds funds to escrow account.
     * Called when a donation or subscription payment is processed.
     * 
     * @param campaignId the campaign ID
     * @param amount the amount to add
     * @throws ValidationException if validation fails
     * @throws BusinessException if operation fails
     */
    public void addFunds(Long campaignId, double amount) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        Validator.validatePositive(amount, "Amount");
        
        try {
            EscrowAccount escrow = escrowRepository.findByCampaign(campaignId);
            
            if (escrow == null) {
                throw new BusinessException("Escrow account not found for campaign");
            }
            
            escrowRepository.addFunds(escrow.getId(), amount);
            
            logger.info("Funds added to escrow: campaignId={}, amount={}, newTotal={}", 
                       campaignId, amount, escrow.getTotalAmount() + amount);
            
        } catch (SQLException e) {
            logger.error("Database error while adding funds to escrow", e);
            throw new BusinessException("Failed to add funds to escrow", e);
        }
    }
    
    /**
     * Releases funds from escrow account.
     * Called when a milestone is approved and funds should be released to campaigner.
     * 
     * Business Rule: Decreases availableAmount and increases releasedAmount.
     * 
     * @param campaignId the campaign ID
     * @param amount the amount to release
     * @param reason the reason for release
     * @throws ValidationException if validation fails
     * @throws BusinessException if operation fails or insufficient funds
     */
    public void releaseFunds(Long campaignId, double amount, String reason) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        Validator.validatePositive(amount, "Amount");
        Validator.validateNonEmpty(reason, "Release reason");
        
        try {
            EscrowAccount escrow = escrowRepository.findByCampaign(campaignId);
            
            if (escrow == null) {
                throw new BusinessException("Escrow account not found for campaign");
            }
            
            // Check if sufficient funds are available
            if (escrow.getAvailableAmount() < amount) {
                throw new BusinessException(
                    String.format("Insufficient funds in escrow. Available: %.2f, Requested: %.2f",
                                escrow.getAvailableAmount(), amount));
            }
            
            // Release funds
            boolean success = escrowRepository.releaseFunds(escrow.getId(), amount);
            
            if (!success) {
                throw new BusinessException("Failed to release funds from escrow");
            }
            
            logger.info("Funds released from escrow: campaignId={}, amount={}, reason={}, remainingAvailable={}", 
                       campaignId, amount, reason, escrow.getAvailableAmount() - amount);
            
        } catch (SQLException e) {
            logger.error("Database error while releasing funds from escrow", e);
            throw new BusinessException("Failed to release funds from escrow", e);
        }
    }
    
    /**
     * Gets available balance in escrow for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return the available balance
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public double getAvailableBalance(Long campaignId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        
        try {
            EscrowAccount escrow = escrowRepository.findByCampaign(campaignId);
            
            if (escrow == null) {
                throw new BusinessException("Escrow account not found for campaign");
            }
            
            double available = escrow.getAvailableAmount();
            logger.debug("Available balance for campaign {}: {}", campaignId, available);
            return available;
            
        } catch (SQLException e) {
            logger.error("Database error while getting available balance", e);
            throw new BusinessException("Failed to retrieve available balance", e);
        }
    }
    
    /**
     * Gets total amount in escrow for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return the total amount
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public double getTotalAmount(Long campaignId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        
        try {
            EscrowAccount escrow = escrowRepository.findByCampaign(campaignId);
            
            if (escrow == null) {
                throw new BusinessException("Escrow account not found for campaign");
            }
            
            double total = escrow.getTotalAmount();
            logger.debug("Total amount for campaign {}: {}", campaignId, total);
            return total;
            
        } catch (SQLException e) {
            logger.error("Database error while getting total amount", e);
            throw new BusinessException("Failed to retrieve total amount", e);
        }
    }
    
    /**
     * Gets released amount from escrow for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return the released amount
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public double getReleasedAmount(Long campaignId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        
        try {
            EscrowAccount escrow = escrowRepository.findByCampaign(campaignId);
            
            if (escrow == null) {
                throw new BusinessException("Escrow account not found for campaign");
            }
            
            double released = escrow.getReleasedAmount();
            logger.debug("Released amount for campaign {}: {}", campaignId, released);
            return released;
            
        } catch (SQLException e) {
            logger.error("Database error while getting released amount", e);
            throw new BusinessException("Failed to retrieve released amount", e);
        }
    }
}
