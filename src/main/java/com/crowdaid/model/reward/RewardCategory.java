package com.crowdaid.model.reward;

/**
 * Enumeration representing reward categories in the reward shop.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public enum RewardCategory {
    /**
     * Digital badges and achievements
     */
    DIGITAL_BADGE("Digital Badge"),
    
    /**
     * Discount vouchers and coupons
     */
    VOUCHER("Voucher"),
    
    /**
     * Physical merchandise items
     */
    MERCHANDISE("Merchandise"),
    
    /**
     * Public recognition items
     */
    RECOGNITION("Recognition"),
    
    /**
     * Exclusive content access
     */
    EXCLUSIVE_CONTENT("Exclusive Content");
    
    private final String displayName;
    
    RewardCategory(String displayName) {
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
