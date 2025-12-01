package com.crowdaid.service;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.Milestone;
import com.crowdaid.model.campaign.MilestoneStatus;
import com.crowdaid.model.voting.Vote;
import com.crowdaid.model.voting.VoteType;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.interfaces.DonationRepository;
import com.crowdaid.repository.interfaces.MilestoneRepository;
import com.crowdaid.repository.interfaces.VoteRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.repository.mysql.MySQLDonationRepository;
import com.crowdaid.repository.mysql.MySQLMilestoneRepository;
import com.crowdaid.repository.mysql.MySQLVoteRepository;
import com.crowdaid.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * VoteService handles milestone voting operations.
 * 
 * Implements UC9 (Vote on Milestone Completion).
 * 
 * Applies GRASP principles:
 * - Controller: Coordinates voting workflow
 * - Information Expert: Vote-related business logic
 * - Low Coupling: Depends on repository interfaces
 * 
 * Business Rule: 60% approval threshold for milestone approval.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class VoteService {
    
    private static final Logger logger = LoggerFactory.getLogger(VoteService.class);
    private static final double APPROVAL_THRESHOLD = 0.60; // 60% approval required
    
    private final VoteRepository voteRepository;
    private final MilestoneRepository milestoneRepository;
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final MilestoneService milestoneService;
    private final EscrowService escrowService;
    private final NotificationService notificationService;
    
    /**
     * Constructor initializing repositories and services.
     */
    public VoteService() {
        this.voteRepository = new MySQLVoteRepository();
        this.milestoneRepository = new MySQLMilestoneRepository();
        this.donationRepository = new MySQLDonationRepository();
        this.campaignRepository = new MySQLCampaignRepository();
        this.milestoneService = new MilestoneService();
        this.escrowService = new EscrowService();
        this.notificationService = new NotificationService();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param voteRepository the vote repository
     * @param milestoneRepository the milestone repository
     * @param donationRepository the donation repository
     * @param campaignRepository the campaign repository
     * @param milestoneService the milestone service
     * @param escrowService the escrow service
     * @param notificationService the notification service
     */
    public VoteService(VoteRepository voteRepository,
                      MilestoneRepository milestoneRepository,
                      DonationRepository donationRepository,
                      CampaignRepository campaignRepository,
                      MilestoneService milestoneService,
                      EscrowService escrowService,
                      NotificationService notificationService) {
        this.voteRepository = voteRepository;
        this.milestoneRepository = milestoneRepository;
        this.donationRepository = donationRepository;
        this.campaignRepository = campaignRepository;
        this.milestoneService = milestoneService;
        this.escrowService = escrowService;
        this.notificationService = notificationService;
    }
    
    /**
     * Casts a vote on a milestone (UC9: Vote on Milestone Completion).
     * 
     * Business Rule: Only donors who donated to the campaign can vote.
     * 
     * @param milestoneId the milestone ID
     * @param donorId the donor's user ID
     * @param voteType the vote type (APPROVE or REJECT)
     * @param comment optional comment
     * @return the created vote
     * @throws ValidationException if validation fails
     * @throws BusinessException if voting fails
     */
    public Vote castVote(Long milestoneId, Long donorId, VoteType voteType, String comment)
            throws ValidationException, BusinessException {
        
        // Validate inputs
        Validator.validatePositive(milestoneId, "Milestone ID");
        Validator.validatePositive(donorId, "Donor ID");
        Validator.validateNotNull(voteType, "Vote type");
        
        try {
            // Verify milestone exists and is under review
            Milestone milestone = milestoneRepository.findById(milestoneId);
            
            if (milestone == null) {
                throw new BusinessException("Milestone not found");
            }
            
            if (milestone.getStatus() != MilestoneStatus.UNDER_REVIEW) {
                throw new BusinessException("Milestone is not under review. Current status: " + 
                                          milestone.getStatus());
            }
            
            // Verify donor has donated to this campaign
            double totalDonation = donationRepository.getTotalDonationByDonorToCampaign(
                donorId, milestone.getCampaignId());
            
            if (totalDonation <= 0) {
                throw new BusinessException("You must donate to the campaign to vote on its milestones");
            }
            
            // Check if donor has already voted
            if (voteRepository.hasVoted(donorId, milestoneId)) {
                throw new BusinessException("You have already voted on this milestone");
            }
            
            // Create vote
            Vote vote = new Vote();
            vote.setMilestoneId(milestoneId);
            vote.setDonorId(donorId);
            vote.setVoteType(voteType);
            vote.setComment(comment);
            
            Vote savedVote = voteRepository.save(vote);
            
            logger.info("Vote cast: id={}, milestoneId={}, donorId={}, voteType={}", 
                       savedVote.getId(), milestoneId, donorId, voteType);
            
            // Check if voting is complete and process results
            processVotingResults(milestoneId);
            
            return savedVote;
            
        } catch (SQLException e) {
            logger.error("Database error while casting vote", e);
            throw new BusinessException("Failed to cast vote", e);
        }
    }
    
    /**
     * Processes voting results for a milestone.
     * Checks if approval threshold is met and releases funds if approved.
     * 
     * @param milestoneId the milestone ID
     * @throws BusinessException if processing fails
     */
    private void processVotingResults(Long milestoneId) throws BusinessException {
        try {
            Milestone milestone = milestoneRepository.findById(milestoneId);
            
            if (milestone == null) {
                throw new BusinessException("Milestone not found");
            }
            
            // Get voting statistics
            Map<String, Object> voteStats = voteRepository.getVoteStatistics(milestoneId);
            int approveCount = (int) voteStats.get("approveCount");
            int rejectCount = (int) voteStats.get("rejectCount");
            int totalVotes = approveCount + rejectCount;
            
            // Get count of eligible voters (donors who contributed to this campaign)
            int eligibleVoters = donationRepository.getUniqueDonorCount(milestone.getCampaignId());
            
            // Check if all eligible voters have voted or minimum threshold reached
            boolean votingComplete = (totalVotes >= eligibleVoters && eligibleVoters > 0) || totalVotes >= 3;
            
            if (votingComplete) {
                double approvalRate = totalVotes > 0 ? (double) approveCount / totalVotes : 0.0;
                
                Campaign campaign = campaignRepository.findById(milestone.getCampaignId());
                
                if (approvalRate >= APPROVAL_THRESHOLD) {
                    // Approve milestone
                    milestoneService.approveMilestone(milestoneId);
                    
                    // Release funds from escrow
                    escrowService.releaseFunds(milestone.getCampaignId(), milestone.getAmount(), 
                        "Milestone approved: " + milestone.getTitle());
                    
                    // Notify campaigner
                    if (campaign != null) {
                        notificationService.notifyMilestoneApproved(
                            campaign.getCampaignerId(), 
                            campaign.getTitle(), 
                            milestone.getTitle(),
                            milestone.getAmount()
                        );
                    }
                    
                    logger.info("Milestone approved by voting: id={}, approvalRate={}, votes={}/{}", 
                               milestoneId, approvalRate, totalVotes, eligibleVoters);
                } else {
                    // Reject milestone
                    milestoneService.rejectMilestone(milestoneId);
                    
                    // Notify campaigner
                    if (campaign != null) {
                        notificationService.notifyMilestoneRejected(
                            campaign.getCampaignerId(), 
                            campaign.getTitle(), 
                            milestone.getTitle()
                        );
                    }
                    
                    logger.info("Milestone rejected by voting: id={}, approvalRate={}, votes={}/{}", 
                               milestoneId, approvalRate, totalVotes, eligibleVoters);
                }
            }
            
        } catch (ValidationException | SQLException e) {
            logger.error("Error processing voting results", e);
            throw new BusinessException("Failed to process voting results", e);
        }
    }
    
    /**
     * Retrieves all votes for a milestone.
     * 
     * @param milestoneId the milestone ID
     * @return list of votes
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public List<Vote> getVotesByMilestone(Long milestoneId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(milestoneId, "Milestone ID");
        
        try {
            List<Vote> votes = voteRepository.findByMilestone(milestoneId);
            logger.debug("Retrieved {} votes for milestone {}", votes.size(), milestoneId);
            return votes;
            
        } catch (SQLException e) {
            logger.error("Database error while finding votes by milestone", e);
            throw new BusinessException("Failed to retrieve votes", e);
        }
    }
    
    /**
     * Gets voting statistics for a milestone.
     * 
     * @param milestoneId the milestone ID
     * @return map containing vote statistics (approveCount, rejectCount, totalVotes, approvalRate)
     * @throws ValidationException if validation fails
     * @throws BusinessException if retrieval fails
     */
    public Map<String, Object> getVoteStatistics(Long milestoneId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(milestoneId, "Milestone ID");
        
        try {
            Map<String, Object> stats = voteRepository.getVoteStatistics(milestoneId);
            logger.debug("Retrieved vote statistics for milestone {}: {}", milestoneId, stats);
            return stats;
            
        } catch (SQLException e) {
            logger.error("Database error while getting vote statistics", e);
            throw new BusinessException("Failed to retrieve vote statistics", e);
        }
    }
    
    /**
     * Checks if a donor has voted on a milestone.
     * 
     * @param donorId the donor's user ID
     * @param milestoneId the milestone ID
     * @return true if donor has voted
     * @throws ValidationException if validation fails
     * @throws BusinessException if check fails
     */
    public boolean hasVoted(Long donorId, Long milestoneId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(donorId, "Donor ID");
        Validator.validatePositive(milestoneId, "Milestone ID");
        
        try {
            boolean voted = voteRepository.hasVoted(donorId, milestoneId);
            logger.debug("Donor {} voted on milestone {}: {}", donorId, milestoneId, voted);
            return voted;
            
        } catch (SQLException e) {
            logger.error("Database error while checking vote status", e);
            throw new BusinessException("Failed to check vote status", e);
        }
    }
    
    /**
     * Checks if a donor is eligible to vote on a milestone.
     * 
     * @param donorId the donor's user ID
     * @param milestoneId the milestone ID
     * @return true if donor is eligible
     * @throws ValidationException if validation fails
     * @throws BusinessException if check fails
     */
    public boolean isEligibleToVote(Long donorId, Long milestoneId) 
            throws ValidationException, BusinessException {
        Validator.validatePositive(donorId, "Donor ID");
        Validator.validatePositive(milestoneId, "Milestone ID");
        
        try {
            Milestone milestone = milestoneRepository.findById(milestoneId);
            
            if (milestone == null) {
                return false;
            }
            
            // Check if donor has donated to the campaign
            double totalDonation = donationRepository.getTotalDonationByDonorToCampaign(
                donorId, milestone.getCampaignId());
            
            // Eligible if donated and hasn't voted yet
            boolean eligible = totalDonation > 0 && !voteRepository.hasVoted(donorId, milestoneId);
            
            logger.debug("Donor {} eligible to vote on milestone {}: {}", donorId, milestoneId, eligible);
            return eligible;
            
        } catch (SQLException e) {
            logger.error("Database error while checking vote eligibility", e);
            throw new BusinessException("Failed to check vote eligibility", e);
        }
    }
}
