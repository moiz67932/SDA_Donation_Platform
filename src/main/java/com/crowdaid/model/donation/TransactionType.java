package com.crowdaid.model.donation;

/**
 * Enumeration representing transaction types.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public enum TransactionType {
    /**
     * One-time donation into escrow
     */
    DONATION_IN("Donation In"),
    
    /**
     * One-time donation (alias for DONATION_IN)
     */
    DONATION("Donation"),
    
    /**
     * Subscription payment into escrow
     */
    SUBSCRIPTION_IN("Subscription In"),
    
    /**
     * Subscription payment (alias for SUBSCRIPTION_IN)
     */
    SUBSCRIPTION("Subscription"),
    
    /**
     * Funds released from escrow to campaigner
     */
    ESCROW_RELEASE("Escrow Release"),
    
    /**
     * Refund to donor
     */
    REFUND("Refund"),
    
    /**
     * Platform fee deduction
     */
    PLATFORM_FEE("Platform Fee"),
    
    /**
     * Credit earned by donor
     */
    CREDIT_EARNED("Credit Earned"),
    
    /**
     * Credit spent by donor
     */
    CREDIT_SPENT("Credit Spent");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
