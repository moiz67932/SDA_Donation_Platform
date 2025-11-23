package com.crowdaid.model.reward;

/**
 * Enumeration representing reward availability status.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public enum RewardStatus {
    /**
     * Reward is available for redemption
     */
    AVAILABLE("Available"),
    
    /**
     * Reward is out of stock
     */
    OUT_OF_STOCK("Out of Stock"),
    
    /**
     * Reward is temporarily disabled
     */
    DISABLED("Disabled");
    
    private final String displayName;
    
    RewardStatus(String displayName) {
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
