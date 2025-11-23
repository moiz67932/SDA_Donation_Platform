package com.crowdaid.model.donation;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.common.BaseEntity;
import com.crowdaid.model.user.Donor;

/**
 * Donation class representing a one-time donation to a campaign.
 * 
 * Related to UC7 (Make One-Time Donation).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Donation extends BaseEntity {
    
    private Long campaignId;
    private Campaign campaign;
    private Long donorId;
    private Donor donor;
    private double amount;
    private boolean anonymous;
    private String message;
    private String transactionReference;
    
    /**
     * Default constructor.
     */
    public Donation() {
        super();
        this.anonymous = false;
    }
    
    /**
     * Constructor with essential fields.
     * 
     * @param campaignId the campaign ID
     * @param donorId the donor's user ID
     * @param amount the donation amount
     * @param anonymous whether the donation is anonymous
     * @param message optional message from donor
     */
    public Donation(Long campaignId, Long donorId, double amount, boolean anonymous, String message) {
        this();
        this.campaignId = campaignId;
        this.donorId = donorId;
        this.amount = amount;
        this.anonymous = anonymous;
        this.message = message;
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
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public boolean isAnonymous() {
        return anonymous;
    }
    
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    
    /**
     * Gets the donor name for display, considering anonymity.
     * 
     * @return donor name or "Anonymous"
     */
    public String getDisplayName() {
        if (anonymous) {
            return "Anonymous";
        }
        return donor != null ? donor.getName() : "Unknown";
    }
    
    @Override
    public String toString() {
        return "Donation{" +
                "id=" + id +
                ", campaignId=" + campaignId +
                ", donorId=" + donorId +
                ", amount=" + amount +
                ", anonymous=" + anonymous +
                ", createdAt=" + createdAt +
                '}';
    }
}
