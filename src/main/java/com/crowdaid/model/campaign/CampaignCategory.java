package com.crowdaid.model.campaign;

/**
 * Enumeration representing campaign categories.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public enum CampaignCategory {
    MEDICAL("Medical"),
    EDUCATION("Education"),
    EMERGENCY("Emergency"),
    COMMUNITY("Community"),
    CREATIVE("Creative"),
    BUSINESS("Business"),
    NONPROFIT("Nonprofit"),
    ENVIRONMENTAL("Environmental"),
    CIVIC("Civic"),
    OTHER("Other");
    
    private final String displayName;
    
    CampaignCategory(String displayName) {
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
