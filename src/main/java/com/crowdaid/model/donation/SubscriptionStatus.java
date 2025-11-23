package com.crowdaid.model.donation;

/**
 * Enumeration representing subscription status.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public enum SubscriptionStatus {
    /**
     * Subscription is active and recurring
     */
    ACTIVE("Active"),
    
    /**
     * Subscription is paused by the donor
     */
    PAUSED("Paused"),
    
    /**
     * Subscription has been cancelled
     */
    CANCELLED("Cancelled"),
    
    /**
     * Subscription expired due to campaign end
     */
    EXPIRED("Expired");
    
    private final String displayName;
    
    SubscriptionStatus(String displayName) {
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
