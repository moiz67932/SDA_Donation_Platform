package com.crowdaid.model.campaign;

import com.crowdaid.model.common.BaseEntity;
import com.crowdaid.model.donation.EscrowAccount;
import com.crowdaid.model.user.Campaigner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Campaign class representing a fundraising campaign.
 * 
 * Related to UC3 (Create Campaign), UC6 (Browse Campaigns), and UC11 (Approve Campaign).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Campaign extends BaseEntity {
    
    private Long campaignerId;
    private Campaigner campaigner;
    private String title;
    private String description;
    private double goalAmount;
    private double collectedAmount;
    private CampaignCategory category;
    private CampaignStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean philanthropic;
    private boolean civic;
    private String imageUrl;
    
    private EscrowAccount escrowAccount;
    private List<Milestone> milestones;
    private List<CampaignUpdate> updates;
    
    /**
     * Default constructor.
     */
    public Campaign() {
        super();
        this.collectedAmount = 0.0;
        this.status = CampaignStatus.PENDING_REVIEW;
        this.milestones = new ArrayList<>();
        this.updates = new ArrayList<>();
    }
    
    /**
     * Constructor with essential fields.
     * 
     * @param campaignerId the campaigner's user ID
     * @param title the campaign title
     * @param description the campaign description
     * @param goalAmount the fundraising goal amount
     * @param category the campaign category
     */
    public Campaign(Long campaignerId, String title, String description, double goalAmount, CampaignCategory category) {
        this();
        this.campaignerId = campaignerId;
        this.title = title;
        this.description = description;
        this.goalAmount = goalAmount;
        this.category = category;
    }
    
    // Getters and Setters
    
    public Long getCampaignerId() {
        return campaignerId;
    }
    
    public void setCampaignerId(Long campaignerId) {
        this.campaignerId = campaignerId;
    }
    
    public Campaigner getCampaigner() {
        return campaigner;
    }
    
    public void setCampaigner(Campaigner campaigner) {
        this.campaigner = campaigner;
        if (campaigner != null) {
            this.campaignerId = campaigner.getId();
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
    
    public double getGoalAmount() {
        return goalAmount;
    }
    
    public void setGoalAmount(double goalAmount) {
        this.goalAmount = goalAmount;
    }
    
    public double getCollectedAmount() {
        return collectedAmount;
    }
    
    public void setCollectedAmount(double collectedAmount) {
        this.collectedAmount = collectedAmount;
    }
    
    public CampaignCategory getCategory() {
        return category;
    }
    
    public void setCategory(CampaignCategory category) {
        this.category = category;
    }
    
    public CampaignStatus getStatus() {
        return status;
    }
    
    public void setStatus(CampaignStatus status) {
        this.status = status;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public boolean isPhilanthropic() {
        return philanthropic;
    }
    
    public void setPhilanthropic(boolean philanthropic) {
        this.philanthropic = philanthropic;
    }
    
    public boolean isCivic() {
        return civic;
    }
    
    public void setCivic(boolean civic) {
        this.civic = civic;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public EscrowAccount getEscrowAccount() {
        return escrowAccount;
    }
    
    public void setEscrowAccount(EscrowAccount escrowAccount) {
        this.escrowAccount = escrowAccount;
    }
    
    public List<Milestone> getMilestones() {
        return milestones;
    }
    
    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }
    
    public List<CampaignUpdate> getUpdates() {
        return updates;
    }
    
    public void setUpdates(List<CampaignUpdate> updates) {
        this.updates = updates;
    }
    
    /**
     * Calculates the progress percentage toward the goal.
     * 
     * @return progress percentage (0-100)
     */
    public double getProgressPercentage() {
        if (goalAmount <= 0) return 0;
        return Math.min(100, (collectedAmount / goalAmount) * 100);
    }
    
    /**
     * Checks if the campaign has reached its goal.
     * 
     * @return true if goal is reached
     */
    public boolean isGoalReached() {
        return collectedAmount >= goalAmount;
    }
    
    /**
     * Checks if the campaign is currently active.
     * 
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return status == CampaignStatus.ACTIVE;
    }
    
    /**
     * Checks if donations earn credits (philanthropic or civic).
     * 
     * @return true if credits are earned
     */
    public boolean earnsCredits() {
        return philanthropic || civic;
    }
    
    @Override
    public String toString() {
        return "Campaign{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", goalAmount=" + goalAmount +
                ", collectedAmount=" + collectedAmount +
                ", status=" + status +
                ", category=" + category +
                '}';
    }
}
