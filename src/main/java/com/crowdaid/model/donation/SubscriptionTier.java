package com.crowdaid.model.donation;

import com.crowdaid.model.common.BaseEntity;

/**
 * SubscriptionTier class representing a predefined subscription tier for a campaign.
 * Contains tier information like name, monthly amount, and benefits.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class SubscriptionTier extends BaseEntity {
    
    private Long campaignId;
    private String tierName;
    private double monthlyAmount;
    private String description;
    private String benefits;
    
    /**
     * Default constructor.
     */
    public SubscriptionTier() {
        super();
    }
    
    /**
     * Constructor with essential fields.
     * 
     * @param campaignId the campaign ID
     * @param tierName the tier name
     * @param monthlyAmount the monthly subscription amount
     * @param description the tier description
     * @param benefits the tier benefits
     */
    public SubscriptionTier(Long campaignId, String tierName, double monthlyAmount, String description, String benefits) {
        this();
        this.campaignId = campaignId;
        this.tierName = tierName;
        this.monthlyAmount = monthlyAmount;
        this.description = description;
        this.benefits = benefits;
    }
    
    /**
     * Constructor for legacy support.
     * 
     * @param name the tier name
     * @param monthlyAmount the monthly subscription amount
     * @param description the tier description
     */
    public SubscriptionTier(String name, double monthlyAmount, String description) {
        this();
        this.tierName = name;
        this.monthlyAmount = monthlyAmount;
        this.description = description;
    }
    
    // Getters and Setters
    
    public Long getCampaignId() {
        return campaignId;
    }
    
    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }
    
    public String getTierName() {
        return tierName;
    }
    
    public void setTierName(String tierName) {
        this.tierName = tierName;
    }
    
    /**
     * Legacy getter for compatibility.
     * @return the tier name
     */
    public String getName() {
        return tierName;
    }
    
    /**
     * Legacy setter for compatibility.
     * @param name the tier name
     */
    public void setName(String name) {
        this.tierName = name;
    }
    
    public double getMonthlyAmount() {
        return monthlyAmount;
    }
    
    public void setMonthlyAmount(double monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getBenefits() {
        return benefits;
    }
    
    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }
    
    /**
     * Gets display text for tier selection.
     * 
     * @return formatted tier display string
     */
    public String getDisplayText() {
        return tierName + " - $" + String.format("%.2f", monthlyAmount) + "/month";
    }
    
    /**
     * Gets predefined subscription tiers (for legacy support).
     * 
     * @return array of subscription tiers
     */
    public static SubscriptionTier[] getDefaultTiers() {
        return new SubscriptionTier[]{
            new SubscriptionTier("Bronze", 10.0, "Basic supporter tier with monthly updates"),
            new SubscriptionTier("Silver", 25.0, "Silver supporter with exclusive updates and recognition"),
            new SubscriptionTier("Gold", 50.0, "Gold supporter with premium benefits and early access")
        };
    }
    
    @Override
    public String toString() {
        return getDisplayText();
    }
}
