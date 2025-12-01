package com.crowdaid.service;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.CampaignStatus;
import com.crowdaid.model.donation.Subscription;
import com.crowdaid.model.donation.SubscriptionStatus;
import com.crowdaid.model.donation.SubscriptionTier;
import com.crowdaid.model.donation.TransactionType;
import com.crowdaid.model.donation.TransactionStatus;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.interfaces.EscrowRepository;
import com.crowdaid.repository.interfaces.SubscriptionRepository;
import com.crowdaid.repository.interfaces.SubscriptionTierRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.repository.mysql.MySQLEscrowRepository;
import com.crowdaid.repository.mysql.MySQLSubscriptionRepository;
import com.crowdaid.repository.mysql.MySQLSubscriptionTierRepository;
import com.crowdaid.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * SubscriptionService handles subscription-based donation operations.
 * 
 * Implements UC8 (Subscribe to Campaign).
 * 
 * Applies GRASP principles:
 * - Controller: Coordinates subscription workflow
 * - Information Expert: Subscription-related business logic
 * - Low Coupling: Depends on repository interfaces
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class SubscriptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
    private static final double CREDIT_EARNING_RATE = 0.01; // 1 credit per 100 units donated
    
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionTierRepository tierRepository;
    private final CampaignRepository campaignRepository;
    private final EscrowRepository escrowRepository;
    private final CreditService creditService;
    private final TransactionService transactionService;
    private final NotificationService notificationService;
    
    /**
     * Constructor initializing repositories and services.
     */
    public SubscriptionService() {
        this.subscriptionRepository = new MySQLSubscriptionRepository();
        this.tierRepository = new MySQLSubscriptionTierRepository();
        this.campaignRepository = new MySQLCampaignRepository();
        this.escrowRepository = new MySQLEscrowRepository();
        this.creditService = new CreditService();
        this.transactionService = new TransactionService();
        this.notificationService = new NotificationService();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param subscriptionRepository the subscription repository
     * @param tierRepository the subscription tier repository
     * @param campaignRepository the campaign repository
     * @param escrowRepository the escrow repository
     * @param creditService the credit service
     * @param transactionService the transaction service
     * @param notificationService the notification service
     */
    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               SubscriptionTierRepository tierRepository,
                               CampaignRepository campaignRepository,
                               EscrowRepository escrowRepository,
                               CreditService creditService,
                               TransactionService transactionService,
                               NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.tierRepository = tierRepository;
        this.campaignRepository = campaignRepository;
        this.escrowRepository = escrowRepository;
        this.creditService = creditService;
        this.transactionService = transactionService;
        this.notificationService = notificationService;
    }
    
    /**
     * Creates a new subscription (UC8: Subscribe to Campaign).
     * 
     * @param campaignId the campaign ID
     * @param donorId the donor's user ID
     * @param tier the subscription tier
     * @param amount the subscription amount per period
     * @return the created subscription
     * @throws ValidationException if validation fails
     * @throws BusinessException if subscription creation fails
     */
    public Subscription subscribe(Long campaignId, Long donorId, SubscriptionTier tier, double amount)
            throws ValidationException, BusinessException {
        
        // Validate inputs
        Validator.validatePositive(campaignId, "Campaign ID");
        Validator.validatePositive(donorId, "Donor ID");
        Validator.validateNotNull(tier, "Subscription tier");
        Validator.validatePositive(amount, "Subscription amount");
        
        try {
            // Verify campaign exists and is active
            Campaign campaign = campaignRepository.findById(campaignId);
            
            if (campaign == null) {
                throw new BusinessException("Campaign not found");
            }
            
            if (campaign.getStatus() != CampaignStatus.ACTIVE) {
                throw new BusinessException("Campaign is not active");
            }
            
            // Check if donor already has an active subscription to this campaign
            if (subscriptionRepository.hasActiveSubscription(donorId, campaignId)) {
                throw new BusinessException("You already have an active subscription to this campaign");
            }
            
            // Create subscription
            Subscription subscription = new Subscription();
            subscription.setCampaignId(campaignId);
            subscription.setDonorId(donorId);
            subscription.setTier(tier);
            subscription.setAmount(amount);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartDate(LocalDate.now());
            subscription.setNextBillingDate(LocalDate.now().plusMonths(1));
            
            Subscription savedSubscription = subscriptionRepository.save(subscription);
            
            // Process initial payment
            processSubscriptionPayment(savedSubscription);
            
            // Send notifications
            notificationService.notifySubscriptionCreated(donorId, campaign.getTitle(), tier, amount);
            notificationService.notifyCampaignerOfSubscription(
                campaign.getCampaignerId(), 
                campaign.getTitle(), 
                tier, 
                amount
            );
            
            logger.info("Subscription created: id={}, campaignId={}, donorId={}, tier={}, amount={}", 
                       savedSubscription.getId(), campaignId, donorId, tier, amount);
            
            return savedSubscription;
            
        } catch (SQLException e) {
            logger.error("Database error while creating subscription", e);
            throw new BusinessException("Failed to create subscription", e);
        }
    }
    
    /**
     * Processes a subscription payment (called on billing cycle).
     * 
     * @param subscription the subscription to process
     * @throws BusinessException if payment processing fails
     */
    public void processSubscriptionPayment(Subscription subscription) throws BusinessException {
        try {
            Campaign campaign = campaignRepository.findById(subscription.getCampaignId());
            
            if (campaign == null) {
                throw new BusinessException("Campaign not found");
            }
            
            // Generate transaction reference
            String transactionReference = "SUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // Add funds to escrow if campaign is escrow-enabled
            if (campaign.isEscrowEnabled()) {
                escrowRepository.addFunds(campaign.getId(), subscription.getAmount());
                logger.debug("Added ${} to escrow for campaign {}", subscription.getAmount(), campaign.getId());
            }
            
            // Update campaign collected amount
            campaign.setCollectedAmount(campaign.getCollectedAmount() + subscription.getAmount());
            campaignRepository.update(campaign);
            
            // Award credits to donor
            double creditsEarned = subscription.getAmount() * CREDIT_EARNING_RATE;
            creditService.addCredits(subscription.getDonorId(), creditsEarned, 
                "Earned from subscription to campaign: " + campaign.getTitle());
            
            // Log transaction
            transactionService.logTransaction(
                subscription.getCampaignId(),
                subscription.getDonorId(), 
                TransactionType.SUBSCRIPTION_IN, 
                subscription.getAmount(), 
                TransactionStatus.SUCCESS,
                "Subscription payment for campaign: " + campaign.getTitle(),
                transactionReference
            );
            
            // Update next billing date
            subscription.setNextBillingDate(subscription.getNextBillingDate().plusMonths(1));
            subscriptionRepository.update(subscription);
            
            logger.info("Subscription payment processed: subscriptionId={}, amount={}, credits={}", 
                       subscription.getId(), subscription.getAmount(), creditsEarned);
            
        } catch (SQLException e) {
            logger.error("Database error while processing subscription payment", e);
            throw new BusinessException("Failed to process subscription payment", e);
        }
    }
    
    /**
     * Gets all subscriptions for a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of subscriptions
     * @throws BusinessException if operation fails
     */
    public List<Subscription> getDonorSubscriptions(Long donorId) throws BusinessException {
        try {
            return subscriptionRepository.findByDonor(donorId);
        } catch (SQLException e) {
            logger.error("Error retrieving subscriptions for donor: donorId={}", donorId, e);
            throw new BusinessException("Failed to retrieve subscriptions", e);
        }
    }
    
    /**
     * Updates subscription status.
     * 
     * @param subscriptionId the subscription ID
     * @param newStatus the new status
     * @throws BusinessException if operation fails
     */
    public void updateSubscriptionStatus(Long subscriptionId, SubscriptionStatus newStatus) throws BusinessException {
        try {
            subscriptionRepository.updateStatus(subscriptionId, newStatus);
            logger.info("Updated subscription {} to status {}", subscriptionId, newStatus);
        } catch (SQLException e) {
            logger.error("Error updating subscription status: subscriptionId={}", subscriptionId, e);
            throw new BusinessException("Failed to update subscription status", e);
        }
    }
    
    /**
     * Cancels a subscription.
     * 
     * @param subscriptionId the subscription ID
     * @param donorId the donor's user ID (for authorization)
     * @throws ValidationException if validation fails
     * @throws BusinessException if cancellation fails
     */
    public void cancelSubscription(Long subscriptionId, Long donorId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(subscriptionId, "Subscription ID");
        Validator.validatePositive(donorId, "Donor ID");
        
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId);
            
            if (subscription == null) {
                throw new BusinessException("Subscription not found");
            }
            
            if (!subscription.getDonorId().equals(donorId)) {
                throw new BusinessException("You are not authorized to cancel this subscription");
            }
            
            if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
                throw new BusinessException("Subscription is not active");
            }
            
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setCancelDate(LocalDate.now());
            subscriptionRepository.update(subscription);
            
            // Send notification
            Campaign campaign = campaignRepository.findById(subscription.getCampaignId());
            if (campaign != null) {
                notificationService.notifySubscriptionCancelled(donorId, campaign.getTitle());
            }
            
            logger.info("Subscription cancelled: id={}, donorId={}", subscriptionId, donorId);
            
        } catch (SQLException e) {
            logger.error("Database error while cancelling subscription", e);
            throw new BusinessException("Failed to cancel subscription", e);
        }
    }
    
    /**
     * Retrieves a subscription by ID.
     * 
     * @param subscriptionId the subscription ID
     * @return the subscription
     * @throws ValidationException if validation fails
     * @throws BusinessException if subscription not found
     */
    public Subscription getSubscriptionById(Long subscriptionId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(subscriptionId, "Subscription ID");
        
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId);
            
            if (subscription == null) {
                logger.warn("Subscription not found: id={}", subscriptionId);
                throw new BusinessException("Subscription not found with ID: " + subscriptionId);
            }
            
            logger.debug("Subscription retrieved: id={}, amount={}", 
                        subscription.getId(), subscription.getAmount());
            return subscription;
            
        } catch (SQLException e) {
            logger.error("Database error while finding subscription", e);
            throw new BusinessException("Failed to retrieve subscription", e);
        }
    }
    
    /**
     * Retrieves all subscriptions for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return list of subscriptions
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Subscription> getSubscriptionsByCampaign(Long campaignId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(campaignId, "Campaign ID");
        
        try {
            List<Subscription> subscriptions = subscriptionRepository.findByCampaign(campaignId);
            logger.debug("Retrieved {} subscriptions for campaign {}", subscriptions.size(), campaignId);
            return subscriptions;
            
        } catch (SQLException e) {
            logger.error("Database error while finding subscriptions by campaign", e);
            throw new BusinessException("Failed to retrieve subscriptions", e);
        }
    }
    
    /**
     * Retrieves all subscriptions by a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of subscriptions
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Subscription> getSubscriptionsByDonor(Long donorId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(donorId, "Donor ID");
        
        try {
            List<Subscription> subscriptions = subscriptionRepository.findByDonor(donorId);
            logger.debug("Retrieved {} subscriptions for donor {}", subscriptions.size(), donorId);
            return subscriptions;
            
        } catch (SQLException e) {
            logger.error("Database error while finding subscriptions by donor", e);
            throw new BusinessException("Failed to retrieve subscriptions", e);
        }
    }
    
    /**
     * Retrieves active subscriptions for a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of active subscriptions
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Subscription> getActiveSubscriptionsByDonor(Long donorId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(donorId, "Donor ID");
        
        try {
            List<Subscription> subscriptions = subscriptionRepository.findActiveByDonor(donorId);
            logger.debug("Retrieved {} active subscriptions for donor {}", subscriptions.size(), donorId);
            return subscriptions;
            
        } catch (SQLException e) {
            logger.error("Database error while finding active subscriptions", e);
            throw new BusinessException("Failed to retrieve subscriptions", e);
        }
    }
    
    // ==================== Subscription Tier Management ====================
    
    /**
     * Creates a new subscription tier for a campaign.
     * 
     * @param campaignId the campaign ID
     * @param tierName the tier name
     * @param monthlyAmount the monthly subscription amount
     * @param description the tier description
     * @param benefits the tier benefits
     * @return the created tier
     * @throws ValidationException if validation fails
     * @throws BusinessException if tier creation fails
     */
    public SubscriptionTier createTier(Long campaignId, String tierName, double monthlyAmount, 
                                       String description, String benefits)
            throws ValidationException, BusinessException {
        
        Validator.validatePositive(campaignId, "Campaign ID");
        Validator.validateNonEmpty(tierName, "Tier name");
        Validator.validatePositive(monthlyAmount, "Monthly amount");
        
        try {
            // Verify campaign exists
            Campaign campaign = campaignRepository.findById(campaignId);
            if (campaign == null) {
                throw new BusinessException("Campaign not found");
            }
            
            // Check if tier name already exists for this campaign
            SubscriptionTier existing = tierRepository.findByCampaignAndName(campaignId, tierName);
            if (existing != null) {
                throw new BusinessException("A tier with this name already exists for this campaign");
            }
            
            // Create tier
            SubscriptionTier tier = new SubscriptionTier();
            tier.setCampaignId(campaignId);
            tier.setTierName(tierName);
            tier.setMonthlyAmount(monthlyAmount);
            tier.setDescription(description);
            tier.setBenefits(benefits);
            
            SubscriptionTier savedTier = tierRepository.save(tier);
            logger.info("Subscription tier created: id={}, campaignId={}, tierName={}", 
                       savedTier.getId(), campaignId, tierName);
            
            return savedTier;
            
        } catch (SQLException e) {
            logger.error("Database error while creating subscription tier", e);
            throw new BusinessException("Failed to create subscription tier", e);
        }
    }
    
    /**
     * Updates an existing subscription tier.
     * 
     * @param tierId the tier ID
     * @param tierName the tier name
     * @param monthlyAmount the monthly subscription amount
     * @param description the tier description
     * @param benefits the tier benefits
     * @throws ValidationException if validation fails
     * @throws BusinessException if tier update fails
     */
    public void updateTier(Long tierId, String tierName, double monthlyAmount, 
                          String description, String benefits)
            throws ValidationException, BusinessException {
        
        Validator.validatePositive(tierId, "Tier ID");
        Validator.validateNonEmpty(tierName, "Tier name");
        Validator.validatePositive(monthlyAmount, "Monthly amount");
        
        try {
            SubscriptionTier tier = tierRepository.findById(tierId);
            if (tier == null) {
                throw new BusinessException("Subscription tier not found");
            }
            
            tier.setTierName(tierName);
            tier.setMonthlyAmount(monthlyAmount);
            tier.setDescription(description);
            tier.setBenefits(benefits);
            
            tierRepository.update(tier);
            logger.info("Subscription tier updated: id={}, tierName={}", tierId, tierName);
            
        } catch (SQLException e) {
            logger.error("Database error while updating subscription tier", e);
            throw new BusinessException("Failed to update subscription tier", e);
        }
    }
    
    /**
     * Deletes a subscription tier.
     * 
     * @param tierId the tier ID
     * @throws ValidationException if validation fails
     * @throws BusinessException if tier deletion fails
     */
    public void deleteTier(Long tierId) throws ValidationException, BusinessException {
        Validator.validatePositive(tierId, "Tier ID");
        
        try {
            // Check if there are active subscriptions using this tier
            int activeSubscriptions = tierRepository.countActiveSubscriptions(tierId);
            if (activeSubscriptions > 0) {
                throw new BusinessException("Cannot delete tier with active subscriptions");
            }
            
            tierRepository.delete(tierId);
            logger.info("Subscription tier deleted: id={}", tierId);
            
        } catch (SQLException e) {
            logger.error("Database error while deleting subscription tier", e);
            throw new BusinessException("Failed to delete subscription tier", e);
        }
    }
    
    /**
     * Gets all subscription tiers for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return list of subscription tiers
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<SubscriptionTier> getTiersByCampaign(Long campaignId) 
            throws ValidationException, BusinessException {
        
        Validator.validatePositive(campaignId, "Campaign ID");
        
        try {
            List<SubscriptionTier> tiers = tierRepository.findByCampaign(campaignId);
            logger.debug("Retrieved {} subscription tiers for campaign {}", tiers.size(), campaignId);
            return tiers;
            
        } catch (SQLException e) {
            logger.error("Database error while finding subscription tiers", e);
            throw new BusinessException("Failed to retrieve subscription tiers", e);
        }
    }
    
    /**
     * Gets a subscription tier by ID.
     * 
     * @param tierId the tier ID
     * @return the subscription tier
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public SubscriptionTier getTierById(Long tierId) throws ValidationException, BusinessException {
        Validator.validatePositive(tierId, "Tier ID");
        
        try {
            SubscriptionTier tier = tierRepository.findById(tierId);
            if (tier == null) {
                throw new BusinessException("Subscription tier not found");
            }
            return tier;
            
        } catch (SQLException e) {
            logger.error("Database error while finding subscription tier", e);
            throw new BusinessException("Failed to retrieve subscription tier", e);
        }
    }
    
    /**
     * Creates a subscription using a tier ID.
     * 
     * @param campaignId the campaign ID
     * @param donorId the donor's user ID
     * @param tierId the subscription tier ID
     * @return the created subscription
     * @throws ValidationException if validation fails
     * @throws BusinessException if subscription creation fails
     */
    public Subscription subscribeWithTier(Long campaignId, Long donorId, Long tierId)
            throws ValidationException, BusinessException {
        
        Validator.validatePositive(campaignId, "Campaign ID");
        Validator.validatePositive(donorId, "Donor ID");
        Validator.validatePositive(tierId, "Tier ID");
        
        try {
            // Get the tier
            SubscriptionTier tier = tierRepository.findById(tierId);
            if (tier == null) {
                throw new BusinessException("Subscription tier not found");
            }
            
            if (!tier.getCampaignId().equals(campaignId)) {
                throw new BusinessException("Tier does not belong to this campaign");
            }
            
            // Verify campaign exists and is active
            Campaign campaign = campaignRepository.findById(campaignId);
            if (campaign == null) {
                throw new BusinessException("Campaign not found");
            }
            
            if (campaign.getStatus() != CampaignStatus.ACTIVE) {
                throw new BusinessException("Campaign is not active");
            }
            
            // Check if donor already has an active subscription to this campaign
            if (subscriptionRepository.hasActiveSubscription(donorId, campaignId)) {
                throw new BusinessException("You already have an active subscription to this campaign");
            }
            
            // Create subscription
            Subscription subscription = new Subscription();
            subscription.setCampaignId(campaignId);
            subscription.setDonorId(donorId);
            subscription.setTierId(tierId);
            subscription.setTierName(tier.getTierName());
            subscription.setMonthlyAmount(tier.getMonthlyAmount());
            subscription.setDescription(tier.getDescription());
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartDate(LocalDate.now());
            subscription.setNextBillingDate(LocalDate.now().plusMonths(1));
            
            Subscription savedSubscription = subscriptionRepository.save(subscription);
            
            // Process initial payment
            processSubscriptionPayment(savedSubscription);
            
            // Send notifications
            notificationService.notifySubscriptionCreated(donorId, campaign.getTitle(), 
                tier.getTierName(), tier.getMonthlyAmount());
            notificationService.notifyCampaignerOfSubscription(
                campaign.getCampaignerId(), 
                campaign.getTitle(), 
                tier.getTierName(), 
                tier.getMonthlyAmount()
            );
            
            logger.info("Subscription created: id={}, campaignId={}, donorId={}, tierId={}, amount={}", 
                       savedSubscription.getId(), campaignId, donorId, tierId, tier.getMonthlyAmount());
            
            return savedSubscription;
            
        } catch (SQLException e) {
            logger.error("Database error while creating subscription", e);
            throw new BusinessException("Failed to create subscription", e);
        }
    }
}
