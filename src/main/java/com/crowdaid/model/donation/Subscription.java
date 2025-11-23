package com.crowdaid.model.donation;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.common.BaseEntity;
import com.crowdaid.model.user.Donor;

/**
 * Subscription class representing a recurring monthly subscription to a campaign.
 * 
 * Related to UC8 (Subscribe to Campaign).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Subscription extends BaseEntity {
    
    private Long campaignId;
    private Campaign campaign;
    private Long donorId;
    private Donor donor;
    private String tierName;
    private double monthlyAmount;
    private SubscriptionStatus status;
    private String description;
    
    /**
     * Default constructor.
     */
    public Subscription() {
        super();
        this.status = SubscriptionStatus.ACTIVE;
    }
    
    /**
     * Constructor with essential fields.
     * 
     * @param campaignId the campaign ID
     * @param donorId the donor's user ID
     * @param tierName the subscription tier name
     * @param monthlyAmount the monthly subscription amount
     */
    public Subscription(Long campaignId, Long donorId, String tierName, double monthlyAmount) {
        this();
        this.campaignId = campaignId;
        this.donorId = donorId;
        this.tierName = tierName;
        this.monthlyAmount = monthlyAmount;
    }
    
    // Getters and Setters
    
    public Long getCampaignId() {
        return campaignId;
    }
    
    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }
    
    public Campaign getCampaign() {
        return campaign;
    }
    
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
        if (campaign != null) {
            this.campaignId = campaign.getId();
        }
    }
    
    public Long getDonorId() {
        return donorId;
    }
    
    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }
    
    public Donor getDonor() {
        return donor;
    }
    
    public void setDonor(Donor donor) {
        this.donor = donor;
        if (donor != null) {
            this.donorId = donor.getId();
        }
    }
    
    public String getTierName() {
        return tierName;
    }
    
    public void setTierName(String tierName) {
        this.tierName = tierName;
    }
    
    public double getMonthlyAmount() {
        return monthlyAmount;
    }
    
    public void setMonthlyAmount(double monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }
    
    public SubscriptionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Checks if the subscription is currently active.
     * 
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }
    
    /**
     * Pauses the subscription.
     */
    public void pause() {
        this.status = SubscriptionStatus.PAUSED;
        this.touch();
    }
    
    /**
     * Cancels the subscription.
     */
    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        this.touch();
    }
    
    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", campaignId=" + campaignId +
                ", donorId=" + donorId +
                ", tierName='" + tierName + '\'' +
                ", monthlyAmount=" + monthlyAmount +
                ", status=" + status +
                '}';
    }
}
