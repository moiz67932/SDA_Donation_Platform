package com.crowdaid.model.campaign;

import com.crowdaid.model.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * VotingPeriod class representing the voting period for a milestone.
 * Donors can vote during this period to approve or reject milestone completion.
 * 
 * Related to UC9 (Vote on Milestone).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class VotingPeriod extends BaseEntity {
    
    private Long milestoneId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
    
    /**
     * Default constructor.
     */
    public VotingPeriod() {
        super();
        this.active = true;
    }
    
    /**
     * Constructor with milestone and duration.
     * 
     * @param milestoneId the milestone ID
     * @param startTime the voting start time
     * @param endTime the voting end time
     */
    public VotingPeriod(Long milestoneId, LocalDateTime startTime, LocalDateTime endTime) {
        this();
        this.milestoneId = milestoneId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    
    public Long getMilestoneId() {
        return milestoneId;
    }
    
    public void setMilestoneId(Long milestoneId) {
        this.milestoneId = milestoneId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Checks if the voting period is currently open.
     * 
     * @return true if voting is open
     */
    public boolean isOpen() {
        LocalDateTime now = LocalDateTime.now();
        return active && now.isAfter(startTime) && now.isBefore(endTime);
    }
    
    /**
     * Checks if the voting period has ended.
     * 
     * @return true if voting has ended
     */
    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }
    
    @Override
    public String toString() {
        return "VotingPeriod{" +
                "id=" + id +
                ", milestoneId=" + milestoneId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", active=" + active +
                '}';
    }
}
