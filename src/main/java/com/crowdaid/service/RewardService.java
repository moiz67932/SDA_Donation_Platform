package com.crowdaid.service;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.reward.Reward;
import com.crowdaid.model.reward.RewardCategory;
import com.crowdaid.model.reward.RewardStatus;
import com.crowdaid.model.reward.Redemption;
import com.crowdaid.model.reward.RedemptionStatus;
import com.crowdaid.repository.interfaces.RedemptionRepository;
import com.crowdaid.repository.interfaces.RewardRepository;
import com.crowdaid.repository.mysql.MySQLRedemptionRepository;
import com.crowdaid.repository.mysql.MySQLRewardRepository;
import com.crowdaid.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * RewardService handles reward shop and redemption operations.
 * 
 * Implements UC10 (Browse Reward Shop) and UC12 (Redeem Reward).
 * 
 * Applies GRASP principles:
 * - Controller: Coordinates reward and redemption workflow
 * - Information Expert: Reward-related business logic
 * - Low Coupling: Depends on repository interfaces
 * 
 * Business Rule: Check stock and credit balance before redemption.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class RewardService {
    
    private static final Logger logger = LoggerFactory.getLogger(RewardService.class);
    private final RewardRepository rewardRepository;
    private final RedemptionRepository redemptionRepository;
    private final CreditService creditService;
    private final NotificationService notificationService;
    
    /**
     * Constructor initializing repositories and services.
     */
    public RewardService() {
        this.rewardRepository = new MySQLRewardRepository();
        this.redemptionRepository = new MySQLRedemptionRepository();
        this.creditService = new CreditService();
        this.notificationService = new NotificationService();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param rewardRepository the reward repository
     * @param redemptionRepository the redemption repository
     * @param creditService the credit service
     * @param notificationService the notification service
     */
    public RewardService(RewardRepository rewardRepository,
                        RedemptionRepository redemptionRepository,
                        CreditService creditService,
                        NotificationService notificationService) {
        this.rewardRepository = rewardRepository;
        this.redemptionRepository = redemptionRepository;
        this.creditService = creditService;
        this.notificationService = notificationService;
    }
    
    /**
     * Browses all available rewards (UC10: Browse Reward Shop).
     * 
     * @return list of available rewards
     * @throws BusinessException if retrieval fails
     */
    public List<Reward> browseAvailableRewards() throws BusinessException {
        try {
            List<Reward> rewards = rewardRepository.findAllAvailable();
            logger.debug("Retrieved {} available rewards", rewards.size());
            return rewards;
            
        } catch (SQLException e) {
            logger.error("Database error while browsing rewards", e);
            throw new BusinessException("Failed to retrieve rewards", e);
        }
    }
    
    /**
     * Retrieves all rewards (admin operation).
     * 
     * @return list of all rewards
     * @throws BusinessException if retrieval fails
     */
    public List<Reward> getAllRewards() throws BusinessException {
        try {
            List<Reward> rewards = rewardRepository.findAll();
            logger.debug("Retrieved {} total rewards", rewards.size());
            return rewards;
            
        } catch (SQLException e) {
            logger.error("Database error while retrieving all rewards", e);
            throw new BusinessException("Failed to retrieve all rewards", e);
        }
    }
    
    /**
     * Retrieves rewards by category.
     * 
     * @param category the reward category
     * @return list of rewards in the category
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Reward> getRewardsByCategory(RewardCategory category) 
            throws ValidationException, BusinessException {
        Validator.validateNotNull(category, "Reward category");
        
        try {
            List<Reward> rewards = rewardRepository.findByCategory(category);
            logger.debug("Retrieved {} rewards for category {}", rewards.size(), category);
            return rewards;
            
        } catch (SQLException e) {
            logger.error("Database error while finding rewards by category", e);
            throw new BusinessException("Failed to retrieve rewards", e);
        }
    }
    
    /**
     * Retrieves a reward by ID.
     * 
     * @param rewardId the reward ID
     * @return the reward
     * @throws ValidationException if validation fails
     * @throws BusinessException if reward not found
     */
    public Reward getRewardById(Long rewardId) throws ValidationException, BusinessException {
        Validator.validatePositive(rewardId, "Reward ID");
        
        try {
            Reward reward = rewardRepository.findById(rewardId);
            
            if (reward == null) {
                logger.warn("Reward not found: id={}", rewardId);
                throw new BusinessException("Reward not found with ID: " + rewardId);
            }
            
            logger.debug("Reward retrieved: id={}, name={}", reward.getId(), reward.getName());
            return reward;
            
        } catch (SQLException e) {
            logger.error("Database error while finding reward", e);
            throw new BusinessException("Failed to retrieve reward", e);
        }
    }
    
    /**
     * Redeems a reward (UC12: Redeem Reward).
     * 
     * Business Rule: Checks stock availability and donor's credit balance.
     * 
     * @param rewardId the reward ID
     * @param donorId the donor's user ID
     * @param shippingAddress the shipping address
     * @return the redemption record
     * @throws ValidationException if validation fails
     * @throws BusinessException if redemption fails
     */
    public Redemption redeemReward(Long rewardId, Long donorId, String shippingAddress)
            throws ValidationException, BusinessException {
        return redeemRewardInternal(rewardId, donorId, shippingAddress);
    }
    
    /**
     * Redeems a reward for a donor (alternate signature).
     * 
     * @param donorId the donor's user ID
     * @param rewardId the reward ID
     * @return the redemption record
     * @throws ValidationException if validation fails
     * @throws BusinessException if redemption fails
     */
    public Redemption redeemReward(Long donorId, Long rewardId) throws ValidationException, BusinessException {
        return redeemRewardInternal(rewardId, donorId, "To be provided");
    }
    
    /**
     * Internal method to handle reward redemption.
     * 
     * @param rewardId the reward ID
     * @param donorId the donor's user ID
     * @param shippingAddress the shipping address
     * @return the redemption record
     * @throws ValidationException if validation fails
     * @throws BusinessException if redemption fails
     */
    private Redemption redeemRewardInternal(Long rewardId, Long donorId, String shippingAddress)
            throws ValidationException, BusinessException {
        
        // Validate inputs
        Validator.validatePositive(rewardId, "Reward ID");
        Validator.validatePositive(donorId, "Donor ID");
        if (shippingAddress != null && !shippingAddress.equals("To be provided")) {
            Validator.validateNonEmpty(shippingAddress, "Shipping address");
        }
        
        try {
            // Verify reward exists
            Reward reward = rewardRepository.findById(rewardId);
            
            if (reward == null) {
                throw new BusinessException("Reward not found");
            }
            
            // Check if reward is available
            if (reward.getStatus() != RewardStatus.AVAILABLE) {
                throw new BusinessException("Reward is not available. Current status: " + reward.getStatus());
            }
            
            // Check stock availability
            if (reward.getStockQuantity() <= 0) {
                throw new BusinessException("Reward is out of stock");
            }
            
            // Check donor's credit balance
            double donorBalance = creditService.getCreditBalance(donorId);
            
            if (donorBalance < reward.getCreditCost()) {
                throw new BusinessException(
                    String.format("Insufficient credits. Required: %.2f, Available: %.2f",
                                reward.getCreditCost(), donorBalance));
            }
            
            // Deduct credits from donor
            creditService.deductCredits(donorId, reward.getCreditCost(), 
                "Redeemed reward: " + reward.getName());
            
            // Decrease stock quantity
            rewardRepository.decreaseStock(rewardId, 1);
            
            // Create redemption record
            Redemption redemption = new Redemption();
            redemption.setRewardId(rewardId);
            redemption.setDonorId(donorId);
            redemption.setCreditsUsed(reward.getCreditCost());
            redemption.setShippingAddress(shippingAddress);
            redemption.setStatus(RedemptionStatus.PENDING);
            
            Redemption savedRedemption = redemptionRepository.save(redemption);
            
            // Send notification
            notificationService.notifyRewardRedeemed(donorId, reward.getName(), reward.getCreditCost());
            
            logger.info("Reward redeemed: id={}, rewardId={}, donorId={}, creditsUsed={}", 
                       savedRedemption.getId(), rewardId, donorId, reward.getCreditCost());
            
            return savedRedemption;
            
        } catch (SQLException e) {
            logger.error("Database error while redeeming reward", e);
            throw new BusinessException("Failed to redeem reward", e);
        }
    }
    
    /**
     * Retrieves all redemptions by a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of redemptions
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Redemption> getRedemptionsByDonor(Long donorId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(donorId, "Donor ID");
        
        try {
            List<Redemption> redemptions = redemptionRepository.findByDonor(donorId);
            logger.debug("Retrieved {} redemptions for donor {}", redemptions.size(), donorId);
            return redemptions;
            
        } catch (SQLException e) {
            logger.error("Database error while finding redemptions by donor", e);
            throw new BusinessException("Failed to retrieve redemptions", e);
        }
    }
    
    /**
     * Updates redemption status (admin operation).
     * 
     * @param redemptionId the redemption ID
     * @param status the new status
     * @param trackingNumber optional tracking number (for shipped status)
     * @throws ValidationException if validation fails
     * @throws BusinessException if update fails
     */
    public void updateRedemptionStatus(Long redemptionId, RedemptionStatus status, String trackingNumber)
            throws ValidationException, BusinessException {
        Validator.validatePositive(redemptionId, "Redemption ID");
        Validator.validateNotNull(status, "Redemption status");
        
        try {
            Redemption redemption = redemptionRepository.findById(redemptionId);
            
            if (redemption == null) {
                throw new BusinessException("Redemption not found");
            }
            
            redemption.setStatus(status);
            if (trackingNumber != null && !trackingNumber.isEmpty()) {
                redemption.setTrackingNumber(trackingNumber);
            }
            
            redemptionRepository.update(redemption);
            
            // Send notification
            notificationService.notifyRedemptionStatusUpdated(
                redemption.getDonorId(), 
                status, 
                trackingNumber
            );
            
            logger.info("Redemption status updated: id={}, status={}, tracking={}", 
                       redemptionId, status, trackingNumber);
            
        } catch (SQLException e) {
            logger.error("Database error while updating redemption status", e);
            throw new BusinessException("Failed to update redemption status", e);
        }
    }
    
    /**
     * Creates a new reward (admin operation).
     * 
     * @param name the reward name
     * @param description the reward description
     * @param category the reward category
     * @param creditCost the credit cost
     * @param stockQuantity the initial stock quantity
     * @param imageUrl the reward image URL
     * @return the created reward
     * @throws ValidationException if validation fails
     * @throws BusinessException if creation fails
     */
    public Reward createReward(String name, String description, RewardCategory category,
                              double creditCost, int stockQuantity, String imageUrl)
            throws ValidationException, BusinessException {
        
        Validator.validateNonEmpty(name, "Reward name");
        Validator.validateNonEmpty(description, "Reward description");
        Validator.validateNotNull(category, "Reward category");
        Validator.validatePositive(creditCost, "Credit cost");
        Validator.validatePositive(stockQuantity, "Stock quantity");
        
        try {
            Reward reward = new Reward();
            reward.setName(name);
            reward.setDescription(description);
            reward.setCategory(category);
            reward.setCreditCost(creditCost);
            reward.setStockQuantity(stockQuantity);
            reward.setImageUrl(imageUrl);
            reward.setStatus(RewardStatus.AVAILABLE);
            
            Reward savedReward = rewardRepository.save(reward);
            
            logger.info("Reward created: id={}, name={}, cost={}", 
                       savedReward.getId(), name, creditCost);
            
            return savedReward;
            
        } catch (SQLException e) {
            logger.error("Database error while creating reward", e);
            throw new BusinessException("Failed to create reward", e);
        }
    }
    
    /**
     * Updates a reward (admin operation).
     * 
     * @param reward the reward with updated information
     * @throws ValidationException if validation fails
     * @throws BusinessException if update fails
     */
    public void updateReward(Reward reward) throws ValidationException, BusinessException {
        Validator.validateNotNull(reward, "Reward");
        Validator.validatePositive(reward.getId(), "Reward ID");
        Validator.validateNonEmpty(reward.getName(), "Reward name");
        Validator.validatePositive(reward.getCreditCost(), "Credit cost");
        
        try {
            rewardRepository.update(reward);
            logger.info("Reward updated: id={}, name={}", reward.getId(), reward.getName());
            
        } catch (SQLException e) {
            logger.error("Database error while updating reward", e);
            throw new BusinessException("Failed to update reward", e);
        }
    }
    
    /**
     * Deletes a reward (admin operation).
     * 
     * @param rewardId the reward ID
     * @throws ValidationException if validation fails
     * @throws BusinessException if deletion fails
     */
    public void deleteReward(Long rewardId) throws ValidationException, BusinessException {
        Validator.validatePositive(rewardId, "Reward ID");
        
        try {
            rewardRepository.delete(rewardId);
            logger.info("Reward deleted: id={}", rewardId);
            
        } catch (SQLException e) {
            logger.error("Database error while deleting reward", e);
            throw new BusinessException("Failed to delete reward", e);
        }
    }
}
