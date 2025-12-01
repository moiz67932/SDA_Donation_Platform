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
    private Long tierId;
    private SubscriptionTier tier;
    private String tierName;
    private double monthlyAmount;
    private SubscriptionStatus status;
    private String description;
    private java.time.LocalDate startDate;
    private java.time.LocalDate nextBillingDate;
    private java.time.LocalDate cancelDate;
    
    /**
     * Default constructor.
     */
    public Subscription() {
        super();
        this.status = SubscriptionStatus.ACTIVE;
        this.startDate = java.time.LocalDate.now();
        this.nextBillingDate = this.startDate.plusMonths(1);
    }
    
    /**
     * Constructor with essential fields.
     * 
     * @param campaignId the campaign ID
     * @param donorId the donor's user ID
     * @param tierId the subscription tier ID
     * @param tierName the subscription tier name
     * @param monthlyAmount the monthly subscription amount
     */
    public Subscription(Long campaignId, Long donorId, Long tierId, String tierName, double monthlyAmount) {
        this();
        this.campaignId = campaignId;
        this.donorId = donorId;
        this.tierId = tierId;
        this.tierName = tierName;
        this.monthlyAmount = monthlyAmount;
    }
    
    /**
     * Constructor for legacy support.
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
    
    public Long getTierId() {
        return tierId;
    }
    
    public void setTierId(Long tierId) {
        this.tierId = tierId;
    }
    
    public SubscriptionTier getTier() {
        return tier;
    }
    
    public void setTier(SubscriptionTier tier) {
        this.tier = tier;
        if (tier != null) {
            this.tierId = tier.getId();
            this.tierName = tier.getTierName();
            this.monthlyAmount = tier.getMonthlyAmount();
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
    
    /**
     * Gets amount (alias for monthlyAmount for compatibility).
     * @return the monthly amount
     */
    public double getAmount() {
        return monthlyAmount;
    }
    
    /**
     * Sets amount (alias for monthlyAmount for compatibility).
     * @param amount the monthly amount
     */
    public void setAmount(double amount) {
        this.monthlyAmount = amount;
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
    
    public java.time.LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(java.time.LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public java.time.LocalDate getNextBillingDate() {
        return nextBillingDate;
    }
    
    public void setNextBillingDate(java.time.LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }
    
    public java.time.LocalDate getCancelDate() {
        return cancelDate;
    }
    
    public void setCancelDate(java.time.LocalDate cancelDate) {
        this.cancelDate = cancelDate;
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
     * Resumes a paused subscription.
     */
    public void resume() {
        if (status == SubscriptionStatus.PAUSED) {
            this.status = SubscriptionStatus.ACTIVE;
            this.touch();
        }
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
        this.cancelDate = java.time.LocalDate.now();
        this.touch();
    }
    
    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + getId() +
                ", campaignId=" + campaignId +
                ", donorId=" + donorId +
                ", tierId=" + tierId +
                ", tierName='" + tierName + '\'' +
                ", monthlyAmount=" + monthlyAmount +
                ", status=" + status +
                ", startDate=" + startDate +
                ", nextBillingDate=" + nextBillingDate +
                '}';
    }
}
