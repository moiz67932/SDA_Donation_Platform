package com.crowdaid.model.donation;

import com.crowdaid.model.common.BaseEntity;

/**
 * EscrowAccount class representing the escrow account for a campaign.
 * Holds donated funds until milestone-based release.
 * 
 * Related to milestone voting and fund release (UC9).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class EscrowAccount extends BaseEntity {
    
    private Long campaignId;
    private double balance;
    
    /**
     * Default constructor.
     */
    public EscrowAccount() {
        super();
        this.balance = 0.0;
    }
    
    /**
     * Constructor with campaign ID.
     * 
     * @param campaignId the campaign ID
     */
    public EscrowAccount(Long campaignId) {
        this();
        this.campaignId = campaignId;
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param id the escrow account ID
     * @param campaignId the campaign ID
     * @param balance the current balance
     */
    public EscrowAccount(Long id, Long campaignId, double balance) {
        super(id);
        this.campaignId = campaignId;
        this.balance = balance;
    }
    
    // Getters and Setters
    
    public Long getCampaignId() {
        return campaignId;
    }
    
    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    /**
     * Adds funds to the escrow account.
     * 
     * @param amount the amount to add
     */
    public void addFunds(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.touch();
        }
    }
    
    /**
     * Releases funds from the escrow account.
     * 
     * @param amount the amount to release
     * @return true if successful, false if insufficient balance
     */
    public boolean releaseFunds(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            this.touch();
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "EscrowAccount{" +
                "id=" + id +
                ", campaignId=" + campaignId +
                ", balance=" + balance +
                '}';
    }
}
