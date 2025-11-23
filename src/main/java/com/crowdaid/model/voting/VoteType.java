package com.crowdaid.model.voting;

/**
 * Enumeration representing vote types for milestone approval.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public enum VoteType {
    /**
     * Approve the milestone completion
     */
    APPROVE("Approve"),
    
    /**
     * Reject the milestone completion
     */
    REJECT("Reject");
    
    private final String displayName;
    
    VoteType(String displayName) {
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
