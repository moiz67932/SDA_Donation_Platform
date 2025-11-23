package com.crowdaid.model.donation;

/**
 * Enumeration representing transaction status.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public enum TransactionStatus {
    /**
     * Transaction is pending processing
     */
    PENDING("Pending"),
    
    /**
     * Transaction completed successfully
     */
    SUCCESS("Success"),
    
    /**
     * Transaction failed
     */
    FAILED("Failed"),
    
    /**
     * Transaction is being processed
     */
    PROCESSING("Processing"),
    
    /**
     * Transaction has been refunded
     */
    REFUNDED("Refunded");
    
    private final String displayName;
    
    TransactionStatus(String displayName) {
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
