package com.crowdaid.model.campaign;

import com.crowdaid.model.common.BaseEntity;

/**
 * CampaignUpdate class representing updates posted by campaigners.
 * Keeps donors informed about campaign progress.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class CampaignUpdate extends BaseEntity {
    
    private Long campaignId;
    private String title;
    private String body;
    
    /**
     * Default constructor.
     */
    public CampaignUpdate() {
        super();
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param campaignId the campaign ID
     * @param title the update title
     * @param body the update body content
     */
    public CampaignUpdate(Long campaignId, String title, String body) {
        super();
        this.campaignId = campaignId;
        this.title = title;
        this.body = body;
    }
    
    // Getters and Setters
    
    public Long getCampaignId() {
        return campaignId;
    }
    
    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    @Override
    public String toString() {
        return "CampaignUpdate{" +
                "id=" + id +
                ", campaignId=" + campaignId +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
