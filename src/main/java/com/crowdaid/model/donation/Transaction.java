package com.crowdaid.model.donation;

import com.crowdaid.model.common.BaseEntity;

/**
 * Transaction class representing a financial transaction in the system.
 * Tracks all money movements including donations, releases, and refunds.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Transaction extends BaseEntity {
    
    private Long escrowId;
    private Long campaignId;
    private Long donorId;
    private double amount;
    private TransactionType type;
    private TransactionStatus status;
    private String reference;
    private String description;
    
    /**
     * Default constructor.
     */
    public Transaction() {
        super();
        this.status = TransactionStatus.PENDING;
    }
    
    /**
     * Constructor with essential fields.
     * 
     * @param campaignId the campaign ID
     * @param donorId the donor ID (can be null for releases)
     * @param amount the transaction amount
     * @param type the transaction type
     */
    public Transaction(Long campaignId, Long donorId, double amount, TransactionType type) {
        this();
        this.campaignId = campaignId;
        this.donorId = donorId;
        this.amount = amount;
        this.type = type;
    }
    
    // Getters and Setters
    
    public Long getEscrowId() {
        return escrowId;
    }
    
    public void setEscrowId(Long escrowId) {
        this.escrowId = escrowId;
    }
    
    public Long getCampaignId() {
        return campaignId;
    }
    
    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }
    
    public Long getDonorId() {
        return donorId;
    }
    
    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Marks the transaction as successful.
     */
    public void markSuccess() {
        this.status = TransactionStatus.SUCCESS;
        this.touch();
    }
    
    /**
     * Marks the transaction as failed.
     */
    public void markFailed() {
        this.status = TransactionStatus.FAILED;
        this.touch();
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", campaignId=" + campaignId +
                ", amount=" + amount +
                ", type=" + type +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
