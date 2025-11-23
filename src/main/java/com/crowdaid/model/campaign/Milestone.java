package com.crowdaid.model.campaign;

import com.crowdaid.model.common.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Milestone class representing a campaign milestone with specific deliverables.
 * Milestones enable smart fund release based on donor voting.
 * 
 * Related to UC4 (Set Milestones), UC5 (Submit Milestone), and UC9 (Vote on Milestone).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Milestone extends BaseEntity {
    
    private Long campaignId;
    private Campaign campaign;
    private String title;
    private String description;
    private double amount;
    private LocalDate expectedDate;
    private MilestoneStatus status;
    private List<Evidence> evidenceList;
    private VotingPeriod votingPeriod;
    
    /**
     * Default constructor.
     */
    public Milestone() {
        super();
        this.status = MilestoneStatus.PENDING;
        this.evidenceList = new ArrayList<>();
    }
    
    /**
     * Constructor with essential fields.
     * 
     * @param campaignId the campaign ID
     * @param title the milestone title
     * @param description the milestone description
     * @param amount the milestone amount
     * @param expectedDate the expected completion date
     */
    public Milestone(Long campaignId, String title, String description, double amount, LocalDate expectedDate) {
        this();
        this.campaignId = campaignId;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.expectedDate = expectedDate;
    }
    
    // Getters and Setters
    
    public Long getCampaignId() {
        return campaignId;
    }
    
    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }
    
    public Campaign getCampaign() {
        return campaign;
    }
    
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
        if (campaign != null) {
            this.campaignId = campaign.getId();
        }
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public LocalDate getExpectedDate() {
        return expectedDate;
    }
    
    public void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }
    
    public MilestoneStatus getStatus() {
        return status;
    }
    
    public void setStatus(MilestoneStatus status) {
        this.status = status;
    }
    
    public List<Evidence> getEvidenceList() {
        return evidenceList;
    }
    
    public void setEvidenceList(List<Evidence> evidenceList) {
        this.evidenceList = evidenceList;
    }
    
    public VotingPeriod getVotingPeriod() {
        return votingPeriod;
    }
    
    public void setVotingPeriod(VotingPeriod votingPeriod) {
        this.votingPeriod = votingPeriod;
    }
    
    /**
     * Adds evidence to this milestone.
     * 
     * @param evidence the evidence to add
     */
    public void addEvidence(Evidence evidence) {
        this.evidenceList.add(evidence);
        evidence.setMilestoneId(this.id);
    }
    
    /**
     * Checks if this milestone is under review.
     * 
     * @return true if status is UNDER_REVIEW
     */
    public boolean isUnderReview() {
        return status == MilestoneStatus.UNDER_REVIEW;
    }
    
    /**
     * Checks if this milestone is completed.
     * 
     * @return true if status is COMPLETED
     */
    public boolean isCompleted() {
        return status == MilestoneStatus.COMPLETED;
    }
    
    @Override
    public String toString() {
        return "Milestone{" +
                "id=" + id +
                ", campaignId=" + campaignId +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                ", expectedDate=" + expectedDate +
                '}';
    }
}
