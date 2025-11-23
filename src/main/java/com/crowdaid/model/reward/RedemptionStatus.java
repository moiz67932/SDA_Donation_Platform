package com.crowdaid.model.reward;

/**
 * Enumeration representing redemption status.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public enum RedemptionStatus {
    /**
     * Redemption is pending processing
     */
    PENDING("Pending"),
    
    /**
     * Redemption has been processed successfully
     */
    COMPLETED("Completed"),
    
    /**
     * Redemption has been cancelled
     */
    CANCELLED("Cancelled"),
    
    /**
     * Redemption failed due to error
     */
    FAILED("Failed");
    
    private final String displayName;
    
    RedemptionStatus(String displayName) {
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
