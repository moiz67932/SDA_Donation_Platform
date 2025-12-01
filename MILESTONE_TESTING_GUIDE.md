# Milestone & Escrow Voting - Testing Guide

## Quick Test Scenarios

### Scenario 1: Create Campaign with Milestones
**Objective**: Verify milestone creation with all validations

**Steps**:
1. Login as Campaigner
2. Create campaign:
   - Title: "Build Community Center"
   - Goal: $100,000
   - End Date: 2025-12-31
   - ✅ Enable Escrow Protection
3. Go to Milestone Management
4. Try to add milestones:

**Test Case 1.1**: ✅ Valid Milestone
```
Title: Foundation Work
Amount: $30,000
Expected Date: 2025-06-15
Expected Result: SUCCESS
```

**Test Case 1.2**: ❌ Amount Exceeds Goal
```
Title: Building Construction
Amount: $80,000 (total would be $110,000)
Expected Result: ERROR - "Total milestone amounts cannot exceed campaign goal"
```

**Test Case 1.3**: ❌ Date in the Past
```
Title: Planning Phase
Amount: $10,000
Expected Date: 2024-01-01
Expected Result: ERROR - "Expected date must be in the future"
```

**Test Case 1.4**: ❌ Date After Campaign End
```
Title: Final Inspection
Amount: $15,000
Expected Date: 2026-01-15 (after 2025-12-31)
Expected Result: ERROR - "Milestone expected date cannot be after campaign end date"
```

---

### Scenario 2: Donation & Escrow
**Objective**: Verify funds go to escrow

**Steps**:
1. Login as Donor
2. Browse campaigns, find "Build Community Center"
3. Donate $500
4. **Expected**:
   - Donation recorded
   - Funds added to campaign's escrow account
   - NOT directly to campaigner wallet

**Verification**:
```sql
SELECT * FROM escrow_accounts WHERE campaign_id = [campaign_id];
-- Should show balance = $500
```

---

### Scenario 3: Submit Milestone Evidence
**Objective**: Trigger voting process

**Steps**:
1. Login as Campaigner
2. Go to Milestone Management
3. Select "Foundation Work" milestone
4. Click "Submit for Voting"
5. Upload evidence:
   - Description: "Foundation completed as per plans"
   - Files: foundation_photos.jpg, inspection_report.pdf
6. Submit

**Expected Results**:
- Milestone status: PENDING → UNDER_REVIEW
- All campaign donors receive voting notification
- Milestone appears in donors' "Voting Requests" screen

---

### Scenario 4: Donor Voting
**Objective**: Test voting system and approval threshold

**Setup**:
- Campaign has 5 donors who donated: $500, $300, $200, $150, $100
- Milestone: "Foundation Work" ($30,000)
- Status: UNDER_REVIEW

**Test Case 4.1**: ✅ Approval (≥60%)
```
Donor 1 ($500): APPROVE
Donor 2 ($300): APPROVE
Donor 3 ($200): APPROVE
Donor 4 ($150): REJECT
Donor 5 ($100): REJECT

Approval Rate: 3/5 = 60%
Expected Result: MILESTONE APPROVED
```

**Test Case 4.2**: ❌ Rejection (<60%)
```
Donor 1 ($500): APPROVE
Donor 2 ($300): APPROVE
Donor 3 ($200): REJECT
Donor 4 ($150): REJECT
Donor 5 ($100): REJECT

Approval Rate: 2/5 = 40%
Expected Result: MILESTONE REJECTED
```

**Test Case 4.3**: ❌ Duplicate Vote Prevention
```
Donor 1 votes APPROVE
Donor 1 tries to vote again
Expected Result: ERROR - "You have already voted on this milestone"
```

**Test Case 4.4**: ❌ Non-Donor Voting
```
User who didn't donate tries to vote
Expected Result: ERROR - "You must donate to the campaign to vote"
```

---

### Scenario 5: Fund Release & Withdrawal
**Objective**: Verify escrow fund release after approval

**Steps**:
1. After milestone approved (≥60% votes)
2. **Automatic Actions**:
   - System calls `escrowService.releaseFunds()`
   - $30,000 deducted from escrow balance
   - Milestone status: APPROVED
3. Login as Campaigner
4. Go to Milestone Management
5. Find "Foundation Work" (status: APPROVED)
6. Click "Withdraw Funds" button
7. **Expected**:
   - Funds transferred to campaigner wallet
   - Milestone status: RELEASED
   - Campaigner's `total_withdrawn` updated

**Verification**:
```sql
-- Check escrow
SELECT balance FROM escrow_accounts WHERE campaign_id = [id];
-- Should be reduced by $30,000

-- Check milestone
SELECT status FROM milestones WHERE id = [milestone_id];
-- Should be 'RELEASED'

-- Check campaigner
SELECT total_withdrawn FROM users WHERE id = [campaigner_id];
-- Should include the $30,000
```

---

### Scenario 6: Edge Cases

**Test Case 6.1**: Campaign Without End Date
```
Create campaign with no end date
Try to create milestone with any future date
Expected Result: SUCCESS (no end date to validate against)
```

**Test Case 6.2**: Multiple Milestones on Same Date
```
Create 3 milestones all with expected date: 2025-06-15
Expected Result: SUCCESS (allowed)
```

**Test Case 6.3**: Voting During Expected Date
```
Milestone expected date: 2025-06-15
Current date: 2025-06-15
Voting still in progress
Expected Result: Voting continues (date is just expected, not enforced)
```

**Test Case 6.4**: Rejected Milestone Resubmission
```
Milestone rejected by voting
Campaigner makes improvements
Submits again for voting
Expected Result: SUCCESS - Status changes REJECTED → UNDER_REVIEW
```

---

## Database Queries for Verification

### Check Milestone Validations
```sql
-- Get campaign with milestones
SELECT 
    c.title AS campaign,
    c.goal_amount,
    c.end_date,
    SUM(m.amount) AS total_milestone_amount,
    COUNT(m.id) AS milestone_count
FROM campaigns c
LEFT JOIN milestones m ON c.id = m.campaign_id
WHERE c.id = [campaign_id]
GROUP BY c.id;

-- Should show: total_milestone_amount ≤ goal_amount
```

### Check Milestone Dates
```sql
SELECT 
    m.title,
    m.expected_date,
    c.end_date AS campaign_end_date,
    CASE 
        WHEN m.expected_date > c.end_date THEN 'INVALID'
        ELSE 'VALID'
    END AS date_validation
FROM milestones m
JOIN campaigns c ON m.campaign_id = c.id
WHERE c.id = [campaign_id];

-- All rows should show 'VALID'
```

### Check Voting Results
```sql
SELECT 
    m.title AS milestone,
    m.status,
    COUNT(CASE WHEN v.vote_type = 'APPROVE' THEN 1 END) AS approve_count,
    COUNT(CASE WHEN v.vote_type = 'REJECT' THEN 1 END) AS reject_count,
    ROUND(
        COUNT(CASE WHEN v.vote_type = 'APPROVE' THEN 1 END) * 100.0 / COUNT(*), 
        2
    ) AS approval_percentage
FROM milestones m
LEFT JOIN votes v ON m.id = v.milestone_id
WHERE m.id = [milestone_id]
GROUP BY m.id;
```

### Check Escrow Balance
```sql
SELECT 
    c.title AS campaign,
    e.balance AS escrow_balance,
    e.released_amount,
    SUM(m.amount) AS total_approved_milestones,
    e.balance - SUM(CASE WHEN m.status = 'APPROVED' THEN m.amount ELSE 0 END) AS remaining_for_release
FROM campaigns c
JOIN escrow_accounts e ON c.id = e.campaign_id
LEFT JOIN milestones m ON c.campaign_id = m.campaign_id
WHERE c.id = [campaign_id]
GROUP BY c.id, e.id;
```

---

## Expected Behaviors Summary

| Action | Expected Validation | Error Message |
|--------|-------------------|---------------|
| Create milestone with amount > remaining goal | ❌ FAIL | "Total milestone amounts cannot exceed campaign goal" |
| Create milestone with past date | ❌ FAIL | "Expected date must be in the future" |
| Create milestone with date > campaign end | ❌ FAIL | "Milestone expected date cannot be after campaign end date" |
| Submit evidence for PENDING milestone | ✅ SUCCESS | Status → UNDER_REVIEW |
| Donor votes on milestone | ✅ SUCCESS | Vote recorded |
| Non-donor tries to vote | ❌ FAIL | "You must donate to the campaign to vote" |
| Donor votes twice | ❌ FAIL | "You have already voted on this milestone" |
| Milestone gets ≥60% approval | ✅ AUTO | Status → APPROVED, Funds released |
| Milestone gets <60% approval | ✅ AUTO | Status → REJECTED |
| Withdraw approved milestone | ✅ SUCCESS | Status → RELEASED |

---

## Manual Testing Script

**Complete End-to-End Test (15 minutes)**

```
1. CREATE CAMPAIGN (2 min)
   - Login: campaigner@test.com
   - Title: "School Library"
   - Goal: $50,000
   - End: 2025-12-31
   - Escrow: ✅ ENABLED

2. CREATE MILESTONES (3 min)
   - M1: "Book Purchase" | $20,000 | 2025-07-01 ✅
   - M2: "Furniture" | $15,000 | 2025-08-15 ✅
   - M3: "Technology" | $15,000 | 2025-09-30 ✅
   - M4: "INVALID" | $20,000 | 2025-07-01 ❌ (exceeds goal)
   - M5: "INVALID" | $10,000 | 2026-01-15 ❌ (after campaign end)

3. DONATIONS (2 min)
   - Donor A: $1,000
   - Donor B: $500
   - Donor C: $300
   - Verify escrow balance: $1,800

4. SUBMIT MILESTONE (2 min)
   - Select "Book Purchase"
   - Evidence: "Purchased 500 books from vendors"
   - Submit → Status: UNDER_REVIEW

5. VOTING (3 min)
   - Donor A: APPROVE ✅
   - Donor B: APPROVE ✅
   - Donor C: REJECT ❌
   - Result: 2/3 = 66.7% → APPROVED ✅

6. WITHDRAWAL (2 min)
   - Campaigner clicks "Withdraw"
   - Verify: Status → RELEASED
   - Escrow balance: $1,800 - $20,000 = -$18,200 (ERROR if negative!)
   - NOTE: Should have more donations first!

7. REPEAT FOR SECOND MILESTONE (1 min)
   - More donations: $25,000 total
   - Submit "Furniture" milestone
   - Vote & Release
```

---

## Automated Test Cases (JUnit)

```java
@Test
public void testMilestoneAmountValidation() {
    // Total milestones = $70,000, Goal = $50,000
    assertThrows(BusinessException.class, () -> {
        milestoneService.defineMilestone(campaignId, "Test", 
            "Desc", 70000.0, LocalDate.now().plusDays(30));
    });
}

@Test
public void testMilestoneDateAfterCampaignEnd() {
    Campaign campaign = new Campaign();
    campaign.setEndDate(LocalDate.of(2025, 12, 31));
    
    assertThrows(ValidationException.class, () -> {
        milestoneService.defineMilestone(campaign.getId(), "Test", 
            "Desc", 10000.0, LocalDate.of(2026, 1, 15));
    });
}

@Test
public void testVotingApprovalThreshold() {
    // 3 approve, 2 reject = 60% → APPROVED
    voteService.castVote(milestoneId, donor1Id, VoteType.APPROVE, null);
    voteService.castVote(milestoneId, donor2Id, VoteType.APPROVE, null);
    voteService.castVote(milestoneId, donor3Id, VoteType.APPROVE, null);
    voteService.castVote(milestoneId, donor4Id, VoteType.REJECT, null);
    voteService.castVote(milestoneId, donor5Id, VoteType.REJECT, null);
    
    Milestone milestone = milestoneRepository.findById(milestoneId);
    assertEquals(MilestoneStatus.APPROVED, milestone.getStatus());
}
```

---

**Document Version**: 1.0  
**Last Updated**: November 26, 2025  
**Status**: Ready for QA Testing
