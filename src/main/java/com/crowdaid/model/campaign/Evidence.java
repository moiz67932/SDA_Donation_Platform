package com.crowdaid.model.campaign;

import com.crowdaid.model.common.BaseEntity;

/**
 * Evidence class representing proof of milestone completion.
 * Campaigners submit evidence when completing milestones.
 * 
 * Related to UC5 (Submit Milestone Completion).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Evidence extends BaseEntity {
    
    private Long milestoneId;
    private String description;
    private String filePath;
    
    /**
     * Default constructor.
     */
    public Evidence() {
        super();
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param milestoneId the milestone ID
     * @param description the evidence description
     * @param filePath the file path or URL
     */
    public Evidence(Long milestoneId, String description, String filePath) {
        super();
        this.milestoneId = milestoneId;
        this.description = description;
        this.filePath = filePath;
    }
    
    // Getters and Setters
    
    public Long getMilestoneId() {
        return milestoneId;
    }
    
    public void setMilestoneId(Long milestoneId) {
        this.milestoneId = milestoneId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    @Override
    public String toString() {
        return "Evidence{" +
                "id=" + id +
                ", milestoneId=" + milestoneId +
                ", description='" + description + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
