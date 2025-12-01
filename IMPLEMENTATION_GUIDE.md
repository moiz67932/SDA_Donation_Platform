# QUICK IMPLEMENTATION GUIDE - Priority Tasks

## 🔥 CRITICAL: Update These Repository Mappers FIRST

### 1. MySQLMilestoneRepository - Add New Fields

Find the `mapResultSetToMilestone()` method and add:

```java
private Milestone mapResultSetToMilestone(ResultSet rs) throws SQLException {
    Milestone milestone = new Milestone();
    milestone.setId(rs.getLong("id"));
    milestone.setCampaignId(rs.getLong("campaign_id"));
    milestone.setTitle(rs.getString("title"));
    milestone.setDescription(rs.getString("description"));
    milestone.setAmount(rs.getDouble("amount"));
    
    Date expectedDate = rs.getDate("expected_date");
    if (expectedDate != null) {
        milestone.setExpectedDate(expectedDate.toLocalDate());
    }
    
    milestone.setStatus(MilestoneStatus.valueOf(rs.getString("status")));
    
    // NEW FIELDS - Add these:
    milestone.setReleasedAmount(rs.getDouble("released_amount"));
    Timestamp releasedAt = rs.getTimestamp("released_at");
    if (releasedAt != null) {
        milestone.setReleasedAt(releasedAt.toLocalDateTime());
    }
    milestone.setWithdrawn(rs.getBoolean("is_withdrawn"));
    
    milestone.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    milestone.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
    return milestone;
}
```

Update INSERT statement to include new fields:
```java
String sql = "INSERT INTO milestones (campaign_id, title, description, amount, " +
             "expected_date, status, released_amount, released_at, is_withdrawn, " +
             "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
```

Update UPDATE statement similarly.

### 2. MySQLUserRepository - Add totalWithdrawn and creditBalance

In `mapResultSetToUser()` method:

```java
user.setTotalWithdrawn(rs.getDouble("total_withdrawn"));
user.setCreditBalance(rs.getDouble("credit_balance"));
```

Update INSERT and UPDATE statements to include these fields.

### 3. MySQLEscrowRepository - Ensure All Fields Mapped

Verify these fields are in your mapper:
```java
escrow.setTotalAmount(rs.getDouble("total_amount"));
escrow.setAvailableAmount(rs.getDouble("available_amount"));
escrow.setReleasedAmount(rs.getDouble("released_amount"));
Timestamp lastWithdrawal = rs.getTimestamp("last_withdrawal_at");
if (lastWithdrawal != null) {
    escrow.setLastWithdrawalAt(lastWithdrawal.toLocalDateTime());
}
```

---

## 🔥 CRITICAL: Implement These Service Methods

### EscrowService.java - Add releaseFundsForMilestone()

```java
/**
 * Releases funds from escrow for an approved milestone.
 * This is the core withdrawal functionality.
 * 
 * @param milestoneId the milestone ID
 * @throws BusinessException if withdrawal fails or validation errors
 */
public void releaseFundsForMilestone(Long milestoneId) throws BusinessException {
    try {
        // 1. Load all related entities
        Milestone milestone = milestoneRepository.findById(milestoneId);
        if (milestone == null) {
            throw new BusinessException("Milestone not found");
        }
        
        Campaign campaign = campaignRepository.findById(milestone.getCampaignId());
        if (campaign == null) {
            throw new BusinessException("Campaign not found");
        }
        
        // 2. Validate preconditions
        if (milestone.getStatus() != MilestoneStatus.APPROVED) {
            throw new BusinessException("Milestone must be approved before withdrawal");
        }
        
        if (milestone.isWithdrawn()) {
            throw new BusinessException("Milestone funds have already been withdrawn");
        }
        
        if (!campaign.isEscrowEnabled()) {
            throw new BusinessException("Campaign does not have escrow enabled");
        }
        
        // 3. Load escrow account
        EscrowAccount escrow = escrowRepository.findByCampaignId(campaign.getId());
        if (escrow == null) {
            throw new BusinessException("Escrow account not found");
        }
        
        double releaseAmount = milestone.getAmount();
        
        if (escrow.getAvailableAmount() < releaseAmount) {
            throw new BusinessException(
                String.format("Insufficient escrow funds. Available: %.2f, Required: %.2f",
                    escrow.getAvailableAmount(), releaseAmount)
            );
        }
        
        // 4. Update escrow account
        escrow.setAvailableAmount(escrow.getAvailableAmount() - releaseAmount);
        escrow.setReleasedAmount(escrow.getReleasedAmount() + releaseAmount);
        escrow.setLastWithdrawalAt(LocalDateTime.now());
        escrowRepository.update(escrow);
        
        // 5. Update milestone
        milestone.setReleasedAmount(releaseAmount);
        milestone.setReleasedAt(LocalDateTime.now());
        milestone.setWithdrawn(true);
        milestone.setStatus(MilestoneStatus.RELEASED);
        milestoneRepository.update(milestone);
        
        // 6. Update campaigner's total withdrawn
        Campaigner campaigner = (Campaigner) userRepository.findById(campaign.getCampaignerId());
        if (campaigner != null) {
            campaigner.setTotalWithdrawn(campaigner.getTotalWithdrawn() + releaseAmount);
            userRepository.update(campaigner);
        }
        
        // 7. Create transaction record
        Transaction transaction = new Transaction();
        transaction.setEscrowId(escrow.getId());
        transaction.setCampaignId(campaign.getId());
        transaction.setAmount(releaseAmount);
        transaction.setType(TransactionType.ESCROW_RELEASE);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription("Escrow release for milestone: " + milestone.getTitle());
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        
        logger.info("Released {} from escrow for milestone {} (campaign {})", 
                   releaseAmount, milestoneId, campaign.getId());
        
    } catch (SQLException e) {
        logger.error("Error releasing escrow funds for milestone {}", milestoneId, e);
        throw new BusinessException("Failed to release escrow funds", e);
    }
}
```

### DonationService.java - Update donate() Method

Add this logic after creating donation record:

```java
// After donation saved successfully:

// Handle escrow deposit
if (campaign.isEscrowEnabled()) {
    escrowService.depositToEscrow(campaign.getId(), amount);
}

// Handle credit earning
if (campaign.earnsCredits()) {
    creditService.grantCreditsForDonation(donor.getId(), campaign.getId(), amount);
}
```

### CreditService.java - Add grantCreditsForDonation() Method

```java
/**
 * Grants credits to a donor for a donation, if the campaign is eligible.
 * 
 * @param donorId the donor's user ID
 * @param campaignId the campaign ID
 * @param amount the donation amount
 * @throws BusinessException if operation fails
 */
public void grantCreditsForDonation(Long donorId, Long campaignId, double amount) 
        throws BusinessException {
    try {
        // Load campaign to check eligibility
        Campaign campaign = campaignRepository.findById(campaignId);
        if (campaign == null || !campaign.earnsCredits()) {
            return; // Campaign not eligible for credits
        }
        
        // Calculate credits (1 per 100 units)
        int credits = (int) Math.floor(amount / CREDIT_RATE);
        
        if (credits > 0) {
            // Add credits to donor
            creditRepository.addCredits(donorId, credits);
            
            // Log transaction
            CreditTransaction ct = new CreditTransaction();
            ct.setDonorId(donorId);
            ct.setAmount(credits);
            ct.setType(CreditTransactionType.EARNED);
            ct.setSource("Donation to campaign: " + campaign.getTitle());
            ct.setReferenceId(campaignId);
            ct.setCreatedAt(LocalDateTime.now());
            creditTransactionRepository.save(ct);
            
            // Update donor's credit balance
            Donor donor = (Donor) userRepository.findById(donorId);
            if (donor != null) {
                donor.setCreditBalance(donor.getCreditBalance() + credits);
                userRepository.update(donor);
            }
            
            logger.info("Awarded {} credits to donor {} for donation of {} to campaign {}", 
                       credits, donorId, amount, campaignId);
        }
    } catch (SQLException e) {
        logger.error("Error granting credits for donation", e);
        throw new BusinessException("Failed to grant credits", e);
    }
}
```

### VoteService.java - Add castVote() Method

```java
/**
 * Casts a vote on a milestone.
 * 
 * @param donorId the donor's user ID
 * @param milestoneId the milestone ID
 * @param voteType APPROVE or REJECT
 * @param comment optional comment
 * @throws BusinessException if voting fails or donor not eligible
 */
public void castVote(Long donorId, Long milestoneId, VoteType voteType, String comment) 
        throws BusinessException {
    try {
        // 1. Load milestone and campaign
        Milestone milestone = milestoneRepository.findById(milestoneId);
        if (milestone == null) {
            throw new BusinessException("Milestone not found");
        }
        
        Campaign campaign = campaignRepository.findById(milestone.getCampaignId());
        
        // 2. Verify donor has donated to this campaign
        List<Donation> donations = donationRepository.findByDonorAndCampaign(donorId, campaign.getId());
        if (donations.isEmpty()) {
            throw new BusinessException("You must donate to this campaign to vote");
        }
        
        // 3. Check voting period is active
        VotingPeriod votingPeriod = votingPeriodRepository.findByMilestoneId(milestoneId);
        if (votingPeriod == null || !votingPeriod.isActive()) {
            throw new BusinessException("Voting period is not active");
        }
        
        if (LocalDateTime.now().isAfter(votingPeriod.getEndTime())) {
            throw new BusinessException("Voting period has ended");
        }
        
        // 4. Check for duplicate vote
        Vote existingVote = voteRepository.findByDonorAndMilestone(donorId, milestoneId);
        if (existingVote != null) {
            throw new BusinessException("You have already voted on this milestone");
        }
        
        // 5. Create vote
        Vote vote = new Vote();
        vote.setMilestoneId(milestoneId);
        vote.setDonorId(donorId);
        vote.setVoteType(voteType);
        vote.setWeight(1.0); // All votes have equal weight
        vote.setComment(comment);
        vote.setCreatedAt(LocalDateTime.now());
        voteRepository.save(vote);
        
        // 6. Check if threshold met
        checkAndApplyVotingResult(milestoneId);
        
        logger.info("Vote cast by donor {} on milestone {}: {}", donorId, milestoneId, voteType);
        
    } catch (SQLException e) {
        logger.error("Error casting vote", e);
        throw new BusinessException("Failed to cast vote", e);
    }
}

/**
 * Checks if voting threshold is met and updates milestone status.
 */
private void checkAndApplyVotingResult(Long milestoneId) throws SQLException {
    List<Vote> votes = voteRepository.findByMilestoneId(milestoneId);
    
    long approvals = votes.stream().filter(v -> v.getVoteType() == VoteType.APPROVE).count();
    long rejections = votes.stream().filter(v -> v.getVoteType() == VoteType.REJECT).count();
    long total = votes.size();
    
    // Simple threshold: 60% approval
    if (total >= 1) { // Minimum 1 vote
        double approvalRate = (double) approvals / total;
        
        if (approvalRate >= 0.6) {
            // Approve milestone
            Milestone milestone = milestoneRepository.findById(milestoneId);
            milestone.setStatus(MilestoneStatus.APPROVED);
            milestoneRepository.update(milestone);
            
            // Deactivate voting period
            VotingPeriod vp = votingPeriodRepository.findByMilestoneId(milestoneId);
            vp.setActive(false);
            votingPeriodRepository.update(vp);
            
            logger.info("Milestone {} approved by voting ({}% approval)", milestoneId, approvalRate * 100);
        }
    }
}
```

### RewardService.java - Add redeemReward() Method

```java
/**
 * Redeems a reward using donor credits.
 * 
 * @param donorId the donor's user ID
 * @param rewardId the reward ID
 * @return the Redemption record
 * @throws BusinessException if redemption fails or insufficient credits
 */
public Redemption redeemReward(Long donorId, Long rewardId) throws BusinessException {
    try {
        // 1. Load donor and reward
        Donor donor = (Donor) userRepository.findById(donorId);
        if (donor == null) {
            throw new BusinessException("Donor not found");
        }
        
        Reward reward = rewardRepository.findById(rewardId);
        if (reward == null) {
            throw new BusinessException("Reward not found");
        }
        
        // 2. Validate reward is available
        if (reward.getStatus() != RewardStatus.AVAILABLE) {
            throw new BusinessException("Reward is not available");
        }
        
        if (reward.getStockQuantity() <= 0) {
            throw new BusinessException("Reward is out of stock");
        }
        
        // 3. Check donor has sufficient credits
        double creditCost = reward.getCreditCost();
        if (donor.getCreditBalance() < creditCost) {
            throw new BusinessException(
                String.format("Insufficient credits. You have %.0f but need %.0f",
                    donor.getCreditBalance(), creditCost)
            );
        }
        
        // 4. Deduct credits
        donor.setCreditBalance(donor.getCreditBalance() - creditCost);
        userRepository.update(donor);
        
        // Log credit transaction
        CreditTransaction ct = new CreditTransaction();
        ct.setDonorId(donorId);
        ct.setAmount((int) -creditCost);
        ct.setType(CreditTransactionType.SPENT);
        ct.setSource("Redemption: " + reward.getName());
        ct.setReferenceId(rewardId);
        ct.setCreatedAt(LocalDateTime.now());
        creditTransactionRepository.save(ct);
        
        // 5. Decrement reward stock
        reward.setStockQuantity(reward.getStockQuantity() - 1);
        if (reward.getStockQuantity() == 0) {
            reward.setStatus(RewardStatus.OUT_OF_STOCK);
        }
        rewardRepository.update(reward);
        
        // 6. Create redemption record
        Redemption redemption = new Redemption();
        redemption.setRewardId(rewardId);
        redemption.setDonorId(donorId);
        redemption.setCreditsSpent(creditCost);
        
        // Set status based on reward category
        if (reward.getCategory() == RewardCategory.DIGITAL_BADGE) {
            redemption.setStatus(RedemptionStatus.FULFILLED);
        } else {
            redemption.setStatus(RedemptionStatus.PENDING);
            redemption.setDeliveryInfo("Please contact admin for physical reward delivery");
        }
        
        redemption.setCreatedAt(LocalDateTime.now());
        redemptionRepository.save(redemption);
        
        logger.info("Reward {} redeemed by donor {} for {} credits", 
                   rewardId, donorId, creditCost);
        
        return redemption;
        
    } catch (SQLException e) {
        logger.error("Error redeeming reward", e);
        throw new BusinessException("Failed to redeem reward", e);
    }
}
```

---

## 🎨 UI QUICK START

### Sample Controller Pattern

```java
package com.crowdaid.controller;

import com.crowdaid.model.user.User;
import com.crowdaid.service.*;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ExampleController {
    
    @FXML private Label welcomeLabel;
    @FXML private Button actionButton;
    
    private final SomeService someService = new SomeService();
    
    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getName());
        }
    }
    
    @FXML
    private void handleAction() {
        try {
            // Call service method
            someService.doSomething();
            AlertUtil.showSuccess("Success", "Operation completed");
            
            // Navigate to another screen
            Stage stage = (Stage) actionButton.getScene().getWindow();
            ViewLoader.loadView("/fxml/next_screen.fxml", stage, "Next Screen");
            
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }
}
```

### Sample FXML Pattern

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<BorderPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.crowdaid.controller.ExampleController"
            stylesheets="@../css/styles.css">
    
    <top>
        <VBox styleClass="header">
            <Label fx:id="welcomeLabel" styleClass="title"/>
        </VBox>
    </top>
    
    <center>
        <VBox spacing="10" styleClass="content">
            <Button fx:id="actionButton" text="Click Me" 
                    onAction="#handleAction" styleClass="primary-button"/>
        </VBox>
    </center>
</BorderPane>
```

---

## 📋 REMAINING TASKS CHECKLIST

### High Priority (Do These First):
- [ ] Update MySQLMilestoneRepository mapper
- [ ] Update MySQLUserRepository mapper  
- [ ] Update MySQLEscrowRepository mapper
- [ ] Implement EscrowService.releaseFundsForMilestone()
- [ ] Update DonationService.donate() for escrow + credits
- [ ] Implement CreditService.grantCreditsForDonation()
- [ ] Implement VoteService.castVote()
- [ ] Implement RewardService.redeemReward()

### Medium Priority:
- [ ] Create all FXML files (start with login, donor dashboard, browse campaigns)
- [ ] Create corresponding controllers
- [ ] Implement CreateCampaignController with escrow/reward checkboxes
- [ ] Implement MilestoneManagementController with withdraw button logic
- [ ] Create styles.css

### Low Priority:
- [ ] Add comprehensive Javadoc
- [ ] Create sample data in BootstrapService
- [ ] Test all 12 use cases end-to-end
- [ ] Polish UI with better styling
- [ ] Add input validation in all forms

---

## 🆘 COMMON ISSUES & FIXES

### Issue: Withdraw button not enabling
**Check:**
1. milestone.getStatus() == MilestoneStatus.APPROVED?
2. !milestone.isWithdrawn()?
3. campaign.isEscrowEnabled()?
4. escrow.getAvailableAmount() >= milestone.getAmount()?

### Issue: Credits not being awarded
**Check:**
1. Campaign has isRewardEligible = true or isPhilanthropic = true?
2. DonationService calls creditService.grantCreditsForDonation()?
3. CreditRepository.addCredits() updates users.credit_balance?
4. Credit calculation: (int)(amount / 100.0) not (amount / 1000)?

### Issue: Voting not working
**Check:**
1. VotingPeriod created with active = true?
2. Donor has donated to the campaign?
3. Vote threshold logic checks percentage correctly?
4. Milestone status updates to APPROVED when threshold met?

---

**KEY REMINDER:** The escrow withdrawal feature with the withdraw button is the MOST IMPORTANT part of this project. Make sure it works flawlessly!

**Good luck with your implementation!** 🚀
