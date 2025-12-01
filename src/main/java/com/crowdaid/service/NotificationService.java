package com.crowdaid.service;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.Milestone;
import com.crowdaid.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NotificationService handles sending notifications and emails.
 * Currently simulates notifications by logging to console.
 * In a production system, this would integrate with email services.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    /**
     * Sends a verification email to a new user.
     * 
     * @param user the user to send verification email to
     */
    public void sendVerificationEmail(User user) {
        logger.info("📧 [SIMULATED EMAIL] Verification email sent to: {}", user.getEmail());
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 VERIFICATION EMAIL");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Verify your CrowdAid account");
        System.out.println("Body: Click the link to verify your account (simulated)");
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    /**
     * Sends a donation receipt email to a donor.
     * 
     * @param donorEmail the donor's email
     * @param campaignTitle the campaign title
     * @param amount the donation amount
     */
    public void sendDonationReceipt(String donorEmail, String campaignTitle, double amount) {
        logger.info("📧 [SIMULATED EMAIL] Donation receipt sent to: {}", donorEmail);
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 DONATION RECEIPT");
        System.out.println("To: " + donorEmail);
        System.out.println("Subject: Thank you for your donation!");
        System.out.println("Campaign: " + campaignTitle);
        System.out.println("Amount: $" + amount);
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    /**
     * Sends a subscription confirmation email.
     * 
     * @param donorEmail the donor's email
     * @param campaignTitle the campaign title
     * @param monthlyAmount the monthly subscription amount
     */
    public void sendSubscriptionConfirmation(String donorEmail, String campaignTitle, double monthlyAmount) {
        logger.info("📧 [SIMULATED EMAIL] Subscription confirmation sent to: {}", donorEmail);
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 SUBSCRIPTION CONFIRMATION");
        System.out.println("To: " + donorEmail);
        System.out.println("Subject: Subscription activated!");
        System.out.println("Campaign: " + campaignTitle);
        System.out.println("Monthly Amount: $" + monthlyAmount);
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    /**
     * Notifies admin of a new campaign pending approval.
     * 
     * @param campaign the campaign awaiting approval
     */
    public void notifyAdminOfNewCampaign(Campaign campaign) {
        logger.info("📧 [SIMULATED NOTIFICATION] Admin notified of new campaign: {}", campaign.getTitle());
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("🔔 ADMIN NOTIFICATION");
        System.out.println("New campaign pending approval:");
        System.out.println("Title: " + campaign.getTitle());
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    /**
     * Notifies campaigner that their campaign was approved.
     * 
     * @param campaignerEmail the campaigner's email
     * @param campaignTitle the campaign title
     */
    public void notifyCampaignerOfApproval(String campaignerEmail, String campaignTitle) {
        logger.info("📧 [SIMULATED EMAIL] Campaign approval notification sent to: {}", campaignerEmail);
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 CAMPAIGN APPROVED");
        System.out.println("To: " + campaignerEmail);
        System.out.println("Subject: Your campaign has been approved!");
        System.out.println("Campaign: " + campaignTitle);
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    /**
     * Notifies donors that a milestone is ready for voting.
     * 
     * @param milestone the milestone under review
     */
    public void notifyDonorsOfMilestoneVoting(Milestone milestone) {
        logger.info("📧 [SIMULATED EMAIL] Milestone voting notification sent to donors for milestone: {}", 
                   milestone.getTitle());
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 MILESTONE READY FOR VOTING");
        System.out.println("Subject: Vote on milestone completion");
        System.out.println("Milestone: " + milestone.getTitle());
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    /**
     * Notifies campaigner of milestone approval.
     * 
     * @param campaignerEmail the campaigner's email
     * @param milestoneTitle the milestone title
     */
    public void notifyCampaignerOfMilestoneApproval(String campaignerEmail, String milestoneTitle) {
        logger.info("📧 [SIMULATED EMAIL] Milestone approval sent to: {}", campaignerEmail);
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 MILESTONE APPROVED");
        System.out.println("To: " + campaignerEmail);
        System.out.println("Subject: Milestone approved - funds released!");
        System.out.println("Milestone: " + milestoneTitle);
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    /**
     * Notifies campaigner of milestone rejection.
     * 
     * @param campaignerEmail the campaigner's email
     * @param milestoneTitle the milestone title
     */
    public void notifyCampaignerOfMilestoneRejection(String campaignerEmail, String milestoneTitle) {
        logger.info("📧 [SIMULATED EMAIL] Milestone rejection sent to: {}", campaignerEmail);
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 MILESTONE REJECTED");
        System.out.println("To: " + campaignerEmail);
        System.out.println("Subject: Milestone requires resubmission");
        System.out.println("Milestone: " + milestoneTitle);
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    /**
     * Sends a vote confirmation to a donor.
     * 
     * @param donorEmail the donor's email
     * @param milestoneTitle the milestone title
     * @param voteType the vote type (APPROVE/REJECT)
     */
    public void sendVoteConfirmation(String donorEmail, String milestoneTitle, String voteType) {
        logger.info("📧 [SIMULATED EMAIL] Vote confirmation sent to: {}", donorEmail);
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 VOTE RECORDED");
        System.out.println("To: " + donorEmail);
        System.out.println("Subject: Your vote has been recorded");
        System.out.println("Milestone: " + milestoneTitle);
        System.out.println("Vote: " + voteType);
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    /**
     * Sends a reward redemption confirmation.
     * 
     * @param donorEmail the donor's email
     * @param rewardName the reward name
     * @param creditsUsed the credits used
     */
    public void sendRedemptionConfirmation(String donorEmail, String rewardName, int creditsUsed) {
        logger.info("📧 [SIMULATED EMAIL] Redemption confirmation sent to: {}", donorEmail);
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 REWARD REDEEMED");
        System.out.println("To: " + donorEmail);
        System.out.println("Subject: Reward redemption confirmed");
        System.out.println("Reward: " + rewardName);
        System.out.println("Credits Used: " + creditsUsed);
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    // Additional notification methods
    
    public void notifyDonationReceived(Long donorId, String campaignTitle, double amount) {
        logger.info("Notification: Donation received - Donor: {}, Campaign: {}, Amount: {}", donorId, campaignTitle, amount);
    }
    
    public void notifyCampaignerOfDonation(Long campaignerId, String donorName, double amount, boolean isAnonymous) {
        logger.info("Notification: Campaigner {} received donation of {} from {}", campaignerId, amount, isAnonymous ? "Anonymous" : donorName);
    }
    
    public void notifySubscriptionCreated(Long donorId, String campaignTitle, Object tier, double amount) {
        logger.info("Notification: Subscription created - Donor: {}, Campaign: {}, Amount: {}", donorId, campaignTitle, amount);
    }
    
    public void notifyCampaignerOfSubscription(Long campaignerId, String donorName, Object tier, double amount) {
        logger.info("Notification: Campaigner {} received subscription of {} from {}", campaignerId, amount, donorName);
    }
    
    public void notifySubscriptionCancelled(Long donorId, String campaignTitle) {
        logger.info("Notification: Subscription cancelled - Donor: {}, Campaign: {}", donorId, campaignTitle);
    }
    
    public void notifyMilestoneApproved(Long campaignerId, String campaignTitle, String milestoneTitle, double amount) {
        logger.info("Notification: Milestone approved - Campaigner: {}, Milestone: {}, Amount released: {}", campaignerId, milestoneTitle, amount);
    }
    
    public void notifyMilestoneRejected(Long campaignerId, String campaignTitle, String milestoneTitle) {
        logger.info("Notification: Milestone rejected - Campaigner: {}, Milestone: {}", campaignerId, milestoneTitle);
    }
    
    public void notifyRewardRedeemed(Long donorId, String rewardName, double creditsUsed) {
        logger.info("Notification: Reward redeemed - Donor: {}, Reward: {}, Credits: {}", donorId, rewardName, creditsUsed);
    }
    
    public void notifyRedemptionStatusUpdated(Long donorId, Object status, String trackingInfo) {
        logger.info("Notification: Redemption status updated - Donor: {}, Status: {}, Tracking: {}", donorId, status, trackingInfo);
    }
}
