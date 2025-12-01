package com.crowdaid.model.campaign;

/**
 * Enumeration representing milestone status.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public enum MilestoneStatus {
    /**
     * Milestone is pending completion
     */
    PENDING("Pending"),
    
    /**
     * Milestone is under review after evidence submission
     */
    UNDER_REVIEW("Under Review"),
    
    /**
     * Milestone has been approved by donor voting
     */
    APPROVED("Approved"),
    
    /**
     * Milestone has been rejected by donor voting
     */
    REJECTED("Rejected"),
    
    /**
     * Milestone is completed (legacy status)
     */
    COMPLETED("Completed"),
    
    /**
     * Milestone funds have been released/withdrawn from escrow
     */
    RELEASED("Released");
    
    private final String displayName;
    
    MilestoneStatus(String displayName) {
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
