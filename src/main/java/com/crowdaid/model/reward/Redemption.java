package com.crowdaid.model.reward;

import com.crowdaid.model.common.BaseEntity;

/**
 * Redemption class representing a reward redemption by a donor.
 * 
 * Related to UC10 (Redeem Credits in Shop).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Redemption extends BaseEntity {
    
    private Long rewardId;
    private Long donorId;
    private double creditsSpent;
    private RedemptionStatus status;
    private String deliveryInfo;
    
    /**
     * Default constructor.
     */
    public Redemption() {
        super();
        this.status = RedemptionStatus.PENDING;
    }
    
    /**
     * Constructor with essential fields.
     * 
     * @param rewardId the reward ID
     * @param donorId the donor's user ID
     * @param creditsSpent the credits spent on redemption
     */
    public Redemption(Long rewardId, Long donorId, double creditsSpent) {
        this();
        this.rewardId = rewardId;
        this.donorId = donorId;
        this.creditsSpent = creditsSpent;
    }
    
    // Getters and Setters
    
    public Long getRewardId() {
        return rewardId;
    }
    
    public void setRewardId(Long rewardId) {
        this.rewardId = rewardId;
    }
    
    public Long getDonorId() {
        return donorId;
    }
    
    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }
    
    public double getCreditsSpent() {
        return creditsSpent;
    }
    
    public void setCreditsSpent(double creditsSpent) {
        this.creditsSpent = creditsSpent;
    }
    
    public RedemptionStatus getStatus() {
        return status;
    }
    
    public void setStatus(RedemptionStatus status) {
        this.status = status;
    }
    
    public String getDeliveryInfo() {
        return deliveryInfo;
    }
    
    public void setDeliveryInfo(String deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
    }
    
    public void setCreditsUsed(double creditsUsed) {
        this.creditsSpent = creditsUsed;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.deliveryInfo = shippingAddress;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        if (this.deliveryInfo == null) {
            this.deliveryInfo = "Tracking: " + trackingNumber;
        } else {
            this.deliveryInfo += "; Tracking: " + trackingNumber;
        }
    }
    
    /**
     * Marks the redemption as completed.
     */
    public void markCompleted() {
        this.status = RedemptionStatus.COMPLETED;
        this.touch();
    }
    
    /**
     * Cancels the redemption.
     */
    public void cancel() {
        this.status = RedemptionStatus.CANCELLED;
        this.touch();
    }
    
    @Override
    public String toString() {
        return "Redemption{" +
                "id=" + id +
                ", rewardId=" + rewardId +
                ", donorId=" + donorId +
                ", creditsSpent=" + creditsSpent +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
