package com.crowdaid.model.campaign;

/**
 * Enumeration representing campaign status throughout its lifecycle.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public enum CampaignStatus {
    /**
     * Campaign is pending administrative review
     */
    PENDING_REVIEW("Pending Review"),
    
    /**
     * Campaign has been approved and is active
     */
    ACTIVE("Active"),
    
    /**
     * Campaign has been rejected by administrator
     */
    REJECTED("Rejected"),
    
    /**
     * Campaign has been suspended due to policy violations
     */
    SUSPENDED("Suspended"),
    
    /**
     * Campaign has successfully reached its goal
     */
    COMPLETED("Completed"),
    
    /**
     * Campaign has ended without reaching its goal
     */
    ENDED("Ended"),
    
    /**
     * Campaign has been cancelled by the campaigner
     */
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    CampaignStatus(String displayName) {
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
