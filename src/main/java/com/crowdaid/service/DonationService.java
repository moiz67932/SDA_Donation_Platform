package com.crowdaid.service;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.CampaignStatus;
import com.crowdaid.model.donation.Donation;
import com.crowdaid.model.donation.TransactionStatus;
import com.crowdaid.model.donation.TransactionType;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.interfaces.DonationRepository;
import com.crowdaid.repository.interfaces.EscrowRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.repository.mysql.MySQLDonationRepository;
import com.crowdaid.repository.mysql.MySQLEscrowRepository;
import com.crowdaid.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * DonationService handles one-time donation operations.
 * 
 * Implements UC7 (Make One-Time Donation).
 * 
 * Applies GRASP principles:
 * - Controller: Coordinates donation workflow
 * - Information Expert: Donation-related business logic
 * - Low Coupling: Depends on repository interfaces
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class DonationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DonationService.class);
    
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final EscrowRepository escrowRepository;
    private final CreditService creditService;
    private final TransactionService transactionService;
    private final NotificationService notificationService;
    
    /**
     * Constructor initializing repositories and services.
     */
    public DonationService() {
        this.donationRepository = new MySQLDonationRepository();
        this.campaignRepository = new MySQLCampaignRepository();
        this.escrowRepository = new MySQLEscrowRepository();
        this.creditService = new CreditService();
        this.transactionService = new TransactionService();
        this.notificationService = new NotificationService();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param donationRepository the donation repository
     * @param campaignRepository the campaign repository
     * @param escrowRepository the escrow repository
     * @param creditService the credit service
     * @param transactionService the transaction service
     * @param notificationService the notification service
     */
    public DonationService(DonationRepository donationRepository, 
                          CampaignRepository campaignRepository,
                          EscrowRepository escrowRepository,
                          CreditService creditService,
                          TransactionService transactionService,
                          NotificationService notificationService) {
        this.donationRepository = donationRepository;
        this.campaignRepository = campaignRepository;
        this.escrowRepository = escrowRepository;
        this.creditService = creditService;
        this.transactionService = transactionService;
        this.notificationService = notificationService;
    }
    
    /**
     * Processes a one-time donation (UC7: Make One-Time Donation).
     * 
     * @param campaignId the campaign ID
     * @param donorId the donor's user ID
     * @param amount the donation amount
     * @param anonymous whether the donation is anonymous
     * @param message optional message from donor
     * @return the created donation
     * @throws ValidationException if validation fails
     * @throws BusinessException if donation processing fails
     */
    public Donation makeDonation(Long campaignId, Long donorId, double amount, 
                                 boolean anonymous, String message)
            throws ValidationException, BusinessException {
        
        // Validate inputs
        Validator.validatePositive(campaignId, "Campaign ID");
        Validator.validatePositive(donorId, "Donor ID");
        Validator.validatePositive(amount, "Donation amount");
        
        try {
            // Verify campaign exists and is active
            Campaign campaign = campaignRepository.findById(campaignId);
            
            if (campaign == null) {
                throw new BusinessException("Campaign not found");
            }
            
            if (campaign.getStatus() != CampaignStatus.ACTIVE) {
                throw new BusinessException("Campaign is not active. Current status: " + campaign.getStatus());
            }
            
            // Generate transaction reference
            String transactionReference = "DON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // Create donation
            Donation donation = new Donation(campaignId, donorId, amount, anonymous, message);
            donation.setTransactionReference(transactionReference);
            
            Donation savedDonation = donationRepository.save(donation);
            
            // Add funds to escrow if campaign is escrow-enabled
            if (campaign.isEscrowEnabled()) {
                escrowRepository.addFunds(campaign.getId(), amount);
                logger.debug("Added ${} to escrow for campaign {}", amount, campaignId);
            }
            
            // Update campaign collected amount
            campaign.setCollectedAmount(campaign.getCollectedAmount() + amount);
            campaignRepository.update(campaign);
            
            // Award credits to donor ONLY if the campaign category is COMMUNITY
            // Award 1 credit per $100 donated
            int creditsEarned = 0;
            if (campaign.getCategory() == com.crowdaid.model.campaign.CampaignCategory.COMMUNITY) {
                creditsEarned = creditService.awardCreditsForDonation(donorId, amount);
                logger.info("Awarded {} credits to donor {} for COMMUNITY donation of ${}", 
                           creditsEarned, donorId, amount);
            } else {
                logger.debug("No credits awarded - campaign category is {} (not COMMUNITY)", 
                           campaign.getCategory());
            }
            
            // Log transaction
            transactionService.logTransaction(
                campaignId,
                donorId, 
                TransactionType.DONATION_IN, 
                amount, 
                TransactionStatus.SUCCESS,
                "Donation to campaign: " + campaign.getTitle(),
                transactionReference
            );
            
            // Send notifications
            notificationService.notifyDonationReceived(donorId, campaign.getTitle(), amount);
            notificationService.notifyCampaignerOfDonation(
                campaign.getCampaignerId(), 
                campaign.getTitle(), 
                amount, 
                anonymous
            );
            
            logger.info("Donation processed: id={}, campaignId={}, donorId={}, amount={}, credits={}", 
                       savedDonation.getId(), campaignId, donorId, amount, creditsEarned);
            
            return savedDonation;
            
        } catch (SQLException e) {
            logger.error("Database error while processing donation", e);
            throw new BusinessException("Failed to process donation", e);
        }
    }
    
    /**
     * Retrieves a donation by ID.
     * 
     * @param donationId the donation ID
     * @return the donation
     * @throws ValidationException if validation fails
     * @throws BusinessException if donation not found
     */
    public Donation getDonationById(Long donationId) throws ValidationException, BusinessException {
        Validator.validatePositive(donationId, "Donation ID");
        
        try {
            Donation donation = donationRepository.findById(donationId);
            
            if (donation == null) {
                logger.warn("Donation not found: id={}", donationId);
                throw new BusinessException("Donation not found with ID: " + donationId);
            }
            
            logger.debug("Donation retrieved: id={}, amount={}", donation.getId(), donation.getAmount());
            return donation;
            
        } catch (SQLException e) {
            logger.error("Database error while finding donation", e);
            throw new BusinessException("Failed to retrieve donation", e);
        }
    }
    
    /**
     * Retrieves all donations for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return list of donations
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Donation> getDonationsByCampaign(Long campaignId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        
        try {
            List<Donation> donations = donationRepository.findByCampaign(campaignId);
            logger.debug("Retrieved {} donations for campaign {}", donations.size(), campaignId);
            return donations;
            
        } catch (SQLException e) {
            logger.error("Database error while finding donations by campaign", e);
            throw new BusinessException("Failed to retrieve donations", e);
        }
    }
    
    /**
     * Retrieves all donations by a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of donations
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Donation> getDonationsByDonor(Long donorId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(donorId, "Donor ID");
        
        try {
            List<Donation> donations = donationRepository.findByDonor(donorId);
            logger.debug("Retrieved {} donations for donor {}", donations.size(), donorId);
            return donations;
            
        } catch (SQLException e) {
            logger.error("Database error while finding donations by donor", e);
            throw new BusinessException("Failed to retrieve donations", e);
        }
    }
    
    /**
     * Gets total donation amount by donor to a specific campaign.
     * 
     * @param donorId the donor's user ID
     * @param campaignId the campaign ID
     * @return total donation amount
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public double getTotalDonationByDonorToCampaign(Long donorId, Long campaignId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(donorId, "Donor ID");
        Validator.validatePositive(campaignId, "Campaign ID");
        
        try {
            double total = donationRepository.getTotalDonationByDonorToCampaign(donorId, campaignId);
            logger.debug("Total donation by donor {} to campaign {}: {}", donorId, campaignId, total);
            return total;
            
        } catch (SQLException e) {
            logger.error("Database error while calculating total donation", e);
            throw new BusinessException("Failed to calculate total donation", e);
        }
    }
    
    /**
     * Gets top donors for a campaign.
     * 
     * @param campaignId the campaign ID
     * @param limit the maximum number of donors to return
     * @return list of donations representing top donors
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Donation> getTopDonors(Long campaignId, int limit) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        Validator.validatePositive(limit, "Limit");
        
        try {
            List<Donation> topDonors = donationRepository.getTopDonors(campaignId, limit);
            logger.debug("Retrieved {} top donors for campaign {}", topDonors.size(), campaignId);
            return topDonors;
            
        } catch (SQLException e) {
            logger.error("Database error while finding top donors", e);
            throw new BusinessException("Failed to retrieve top donors", e);
        }
    }
}
