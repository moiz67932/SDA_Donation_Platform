package com.crowdaid.model.voting;

import com.crowdaid.model.common.BaseEntity;

/**
 * Vote class representing a donor's vote on a milestone.
 * Votes are weighted by donation amount and determine milestone approval.
 * 
 * Related to UC9 (Vote on Milestone).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Vote extends BaseEntity {
    
    private Long milestoneId;
    private Long donorId;
    private VoteType voteType;
    private double weight;
    private String comment;
    
    /**
     * Default constructor.
     */
    public Vote() {
        super();
        this.weight = 1.0;
    }
    
    /**
     * Constructor with essential fields.
     * 
     * @param milestoneId the milestone ID
     * @param donorId the donor's user ID
     * @param voteType the vote type (APPROVE or REJECT)
     * @param weight the vote weight based on donation amount
     */
    public Vote(Long milestoneId, Long donorId, VoteType voteType, double weight) {
        this();
        this.milestoneId = milestoneId;
        this.donorId = donorId;
        this.voteType = voteType;
        this.weight = weight;
    }
    
    // Getters and Setters
    
    public Long getMilestoneId() {
        return milestoneId;
    }
    
    public void setMilestoneId(Long milestoneId) {
        this.milestoneId = milestoneId;
    }
    
    public Long getDonorId() {
        return donorId;
    }
    
    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }
    
    public VoteType getVoteType() {
        return voteType;
    }
    
    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * Checks if this is an approval vote.
     * 
     * @return true if vote type is APPROVE
     */
    public boolean isApproval() {
        return voteType == VoteType.APPROVE;
    }
    
    @Override
    public String toString() {
        return "Vote{" +
                "id=" + id +
                ", milestoneId=" + milestoneId +
                ", donorId=" + donorId +
                ", voteType=" + voteType +
                ", weight=" + weight +
                ", createdAt=" + createdAt +
                '}';
    }
}
