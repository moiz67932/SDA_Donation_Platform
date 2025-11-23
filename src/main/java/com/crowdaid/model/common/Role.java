package com.crowdaid.model.common;

/**
 * Enumeration representing user roles in the CrowdAid platform.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public enum Role {
    /**
     * Donor role - users who make donations and subscriptions
     */
    DONOR("Donor"),
    
    /**
     * Campaigner role - users who create and manage fundraising campaigns
     */
    CAMPAIGNER("Campaigner"),
    
    /**
     * Administrator role - users who manage the platform and approve campaigns
     */
    ADMIN("Administrator");
    
    private final String displayName;
    
    Role(String displayName) {
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
