# Milestone & Escrow Voting - Complete Workflow

## System Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                    CROWDAID ESCROW SYSTEM                            │
│  Secure milestone-based fund release with donor voting governance   │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Actor Roles

| Actor | Responsibilities |
|-------|-----------------|
| **Campaigner** | Creates campaign, defines milestones, submits evidence, withdraws funds |
| **Donor** | Makes donations, votes on milestone completion |
| **System** | Holds funds in escrow, processes votes, releases funds automatically |

---

## Complete Workflow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 1: CAMPAIGN SETUP                                                     │
└─────────────────────────────────────────────────────────────────────────────┘

    Campaigner
        │
        ├─→ Creates Campaign
        │     • Title: "Build Community Center"
        │     • Goal: $100,000
        │     • End Date: 2025-12-31
        │     • ☑️ Enable Escrow Protection
        │
        ├─→ Defines Milestones (3 milestones)
        │     
        │     Milestone 1: "Foundation Work"
        │     ├─ Amount: $30,000 ✅ (≤ $100,000)
        │     ├─ Expected: 2025-06-15 ✅ (≤ 2025-12-31)
        │     └─ Status: PENDING
        │     
        │     Milestone 2: "Building Construction"
        │     ├─ Amount: $50,000 ✅ (total: $80,000 ≤ $100,000)
        │     ├─ Expected: 2025-09-30 ✅ (≤ 2025-12-31)
        │     └─ Status: PENDING
        │     
        │     Milestone 3: "Interior Finishing"
        │     ├─ Amount: $20,000 ✅ (total: $100,000 ≤ $100,000)
        │     ├─ Expected: 2025-11-30 ✅ (≤ 2025-12-31)
        │     └─ Status: PENDING
        │
        └─→ Campaign Status: ACTIVE (after admin approval)


┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 2: DONATIONS & ESCROW                                                 │
└─────────────────────────────────────────────────────────────────────────────┘

    Donor A                    Donor B                    Donor C
       │                          │                          │
       ├─→ Donates $5,000        ├─→ Donates $3,000        ├─→ Donates $2,000
       │                          │                          │
       └─────────────────────────┴──────────────────────────┘
                                  │
                                  ↓
                    ┌─────────────────────────────┐
                    │   ESCROW ACCOUNT            │
                    │   Campaign: Community Ctr   │
                    │   Balance: $10,000          │
                    │   Released: $0              │
                    │   Available: $10,000        │
                    └─────────────────────────────┘
                                  │
                         Funds held securely
                         until milestone approval


┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 3: MILESTONE COMPLETION & EVIDENCE SUBMISSION                         │
└─────────────────────────────────────────────────────────────────────────────┘

    Campaigner (2025-06-20)
        │
        │ Foundation work completed!
        │
        ├─→ Goes to "Milestone Management"
        │
        ├─→ Selects "Foundation Work" (Status: PENDING)
        │
        ├─→ Clicks "Submit for Voting"
        │
        ├─→ Uploads Evidence:
        │     • Description: "Foundation completed per specifications"
        │     • Photos: foundation_1.jpg, foundation_2.jpg
        │     • Inspection Report: structural_inspection.pdf
        │
        └─→ Submits
              │
              ↓
        ┌─────────────────────────────────┐
        │ MILESTONE STATUS CHANGE         │
        │ PENDING → UNDER_REVIEW          │
        └─────────────────────────────────┘
              │
              ↓
        ┌─────────────────────────────────┐
        │ SYSTEM TRIGGERS VOTING PERIOD   │
        │ • All campaign donors notified  │
        │ • Voting requests created       │
        └─────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 4: DONOR VOTING                                                       │
└─────────────────────────────────────────────────────────────────────────────┘

    ┌───────────────────────┐    ┌───────────────────────┐    ┌───────────────────────┐
    │ Donor A ($5,000)      │    │ Donor B ($3,000)      │    │ Donor C ($2,000)      │
    ├───────────────────────┤    ├───────────────────────┤    ├───────────────────────┤
    │ 1. Logs in            │    │ 1. Logs in            │    │ 1. Logs in            │
    │ 2. Goes to "Voting    │    │ 2. Goes to "Voting    │    │ 2. Goes to "Voting    │
    │    Requests"          │    │    Requests"          │    │    Requests"          │
    │ 3. Sees milestone:    │    │ 3. Sees milestone:    │    │ 3. Sees milestone:    │
    │    "Foundation Work"  │    │    "Foundation Work"  │    │    "Foundation Work"  │
    │ 4. Views evidence     │    │ 4. Views evidence     │    │ 4. Views evidence     │
    │    • Photos ✓         │    │    • Photos ✓         │    │    • Photos ✓         │
    │    • Report ✓         │    │    • Report ✓         │    │    • Report ✗         │
    │ 5. Decision:          │    │ 5. Decision:          │    │ 5. Decision:          │
    │    ✅ APPROVE         │    │    ✅ APPROVE         │    │    ❌ REJECT          │
    │                       │    │                       │    │                       │
    │ Comment:              │    │ Comment:              │    │ Comment:              │
    │ "Looks solid!"        │    │ "Great work!"         │    │ "Need more detail"    │
    └───────────────────────┘    └───────────────────────┘    └───────────────────────┘
              │                            │                            │
              └────────────────────────────┴────────────────────────────┘
                                           │
                                           ↓
                            ┌──────────────────────────────┐
                            │   VOTING RESULTS             │
                            │   Total Votes: 3             │
                            │   APPROVE: 2 (66.7%)         │
                            │   REJECT: 1 (33.3%)          │
                            │                              │
                            │   Threshold: 60%             │
                            │   Result: ✅ APPROVED        │
                            └──────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 5: AUTOMATIC FUND RELEASE                                             │
└─────────────────────────────────────────────────────────────────────────────┘

    System (Automatically triggered after voting completes)
        │
        ├─→ Checks approval rate: 66.7% ≥ 60% ✅
        │
        ├─→ Calls: milestoneService.approveMilestone()
        │     └─→ Milestone Status: UNDER_REVIEW → APPROVED
        │
        ├─→ Calls: escrowService.releaseFunds()
        │     └─→ Deducts $30,000 from escrow balance
        │
        ├─→ Escrow Account Updated:
        │     • Balance: $10,000 → $10,000 (unchanged - demo scenario)
        │     • Released: $0 → $30,000
        │     • Available: $10,000 → $10,000
        │     
        │     NOTE: In real scenario, need $30,000 in escrow!
        │     Donations should reach milestone amount before withdrawal.
        │
        └─→ Notification sent to Campaigner
              "Milestone 'Foundation Work' approved! Funds ready for withdrawal."


┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 6: CAMPAIGNER WITHDRAWAL                                              │
└─────────────────────────────────────────────────────────────────────────────┘

    Campaigner
        │
        ├─→ Logs in & goes to "Milestone Management"
        │
        ├─→ Sees Milestone "Foundation Work"
        │     Status: APPROVED
        │     Amount: $30,000
        │     [Withdraw Button: ENABLED ✅]
        │
        ├─→ Clicks "Withdraw Funds"
        │
        ├─→ Confirmation Dialog:
        │     "Withdraw $30,000 for milestone 'Foundation Work'?"
        │     [Yes] [No]
        │
        └─→ Clicks [Yes]
              │
              ↓
        ┌─────────────────────────────────────┐
        │ SYSTEM PROCESSES WITHDRAWAL         │
        │ 1. Verify milestone is APPROVED     │
        │ 2. Transfer funds to campaigner     │
        │ 3. Update milestone: APPROVED →     │
        │    RELEASED                         │
        │ 4. Update campaigner total_withdrawn│
        └─────────────────────────────────────┘
              │
              ↓
        ┌─────────────────────────────────────┐
        │ FINAL STATE                         │
        │ • Milestone: RELEASED               │
        │ • Campaigner Wallet: +$30,000       │
        │ • Total Withdrawn: $30,000          │
        │ • Escrow Balance: Reduced           │
        └─────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────────┐
│ REPEAT FOR REMAINING MILESTONES                                             │
└─────────────────────────────────────────────────────────────────────────────┘

    Milestone 2: "Building Construction" ($50,000)
    Milestone 3: "Interior Finishing" ($20,000)
    
    Each follows the same process:
    1. Complete work
    2. Submit evidence
    3. Donors vote (≥60% approval required)
    4. If approved: Funds released automatically
    5. Campaigner withdraws
    6. Milestone status: RELEASED
```

---

## Validation Rules Applied at Each Phase

### Phase 1: Campaign Setup
```
✅ Milestone amount validation
   - Each milestone amount > 0
   - Total milestone amounts ≤ Campaign goal amount
   
✅ Milestone date validation
   - Expected date > Today
   - Expected date ≤ Campaign end date (if set)
   
✅ Escrow requirement
   - Milestones can only be created if escrow is enabled
```

### Phase 2: Donations
```
✅ Escrow routing
   - All donations go to escrow account
   - NOT directly to campaigner wallet
   
✅ Donor eligibility
   - Donors who donate become eligible to vote
```

### Phase 3: Evidence Submission
```
✅ Milestone status check
   - Only PENDING or REJECTED milestones can be submitted
   
✅ Evidence requirement
   - At least one evidence file required
   - Description required
```

### Phase 4: Voting
```
✅ Voter eligibility
   - Only campaign donors can vote
   - Must have donated > $0 to the campaign
   
✅ Duplicate prevention
   - Each donor can vote only once per milestone
   
✅ Milestone status
   - Only UNDER_REVIEW milestones can be voted on
```

### Phase 5: Fund Release
```
✅ Approval threshold
   - Requires ≥60% APPROVE votes
   - Minimum 3 total votes (configurable)
   
✅ Automatic processing
   - If approved: Release funds + Status → APPROVED
   - If rejected: Status → REJECTED
   
✅ Escrow balance check
   - Sufficient funds must be available in escrow
```

### Phase 6: Withdrawal
```
✅ Milestone status
   - Only APPROVED milestones can be withdrawn
   
✅ State transition
   - Status changes: APPROVED → RELEASED
   - Prevents double-withdrawal
   
✅ Accounting
   - Campaigner total_withdrawn updated
   - Transaction logged
```

---

## Status Lifecycle Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ MILESTONE STATUS TRANSITIONS                                                │
└─────────────────────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │ PENDING  │  ← Initial state when milestone is created
    └────┬─────┘
         │
         │ Campaigner submits evidence
         ↓
    ┌──────────────┐
    │ UNDER_REVIEW │  ← Voting period active
    └──────┬───────┘
           │
           ├─────────────────┬─────────────────┐
           │                 │                 │
           │ ≥60% APPROVE    │ <60% APPROVE    │
           ↓                 ↓                 │
    ┌──────────┐      ┌──────────┐           │
    │ APPROVED │      │ REJECTED │           │
    └────┬─────┘      └────┬─────┘           │
         │                 │                 │
         │                 │ Can resubmit    │
         │                 └─────────────────┘
         │
         │ Campaigner withdraws funds
         ↓
    ┌──────────┐
    │ RELEASED │  ← Final state (funds withdrawn)
    └──────────┘


NOTES:
• PENDING: Waiting for campaigner to complete work
• UNDER_REVIEW: Donors are voting (evidence submitted)
• APPROVED: Passed vote, funds released to campaigner wallet (ready to withdraw)
• REJECTED: Failed vote, can resubmit with improvements
• RELEASED: Funds withdrawn, milestone complete
```

---

## Error Scenarios & Handling

### Scenario 1: Insufficient Funds in Escrow
```
Problem: Campaigner tries to withdraw $30,000 but escrow has only $10,000

Flow:
1. Milestone approved by voting
2. System attempts escrowService.releaseFunds($30,000)
3. Escrow check fails: balance ($10,000) < amount ($30,000)
4. Transaction rolled back
5. Error logged: "Insufficient funds in escrow"

Solution:
• Campaign needs more donations before milestone can be fully funded
• Partial releases not supported in current version
• Campaigner informed to wait for more donations
```

### Scenario 2: Milestone Rejected by Voters
```
Problem: Only 40% of donors approved the milestone

Flow:
1. Voting completes: 2 APPROVE, 3 REJECT (40% approval)
2. System calls milestoneService.rejectMilestone()
3. Status: UNDER_REVIEW → REJECTED
4. Funds remain in escrow (not released)
5. Campaigner notified: "Milestone rejected, please improve and resubmit"

Solution:
• Campaigner reviews voter comments
• Makes improvements to work
• Resubmits evidence
• Status changes: REJECTED → UNDER_REVIEW
• New voting period starts
```

### Scenario 3: No Donors to Vote
```
Problem: Campaign has $0 in donations, no donors to vote

Flow:
1. Campaigner submits milestone for voting
2. Status: PENDING → UNDER_REVIEW
3. No voting requests created (no donors)
4. Voting never completes

Solution:
• System should validate: Campaign has at least 1 donor before allowing submission
• Or: Auto-approve if no donors (not recommended for escrow)
• Recommendation: Add validation in SubmitMilestoneController
```

---

## Key Takeaways

### ✅ Implemented & Working
1. **Milestone amount cannot exceed campaign goal** - Validated in service layer
2. **Milestone date cannot be in past** - Validated in service & controller
3. **Milestone date cannot be after campaign end** - ✅ **NEWLY ADDED**
4. **Only campaign donors can vote** - Enforced in VoteService
5. **One vote per donor per milestone** - Database constraint
6. **60% approval threshold** - Implemented in vote processing
7. **Automatic fund release upon approval** - Integrated with escrow service
8. **Complete status lifecycle tracking** - PENDING → UNDER_REVIEW → APPROVED/REJECTED → RELEASED

### 🔒 Security Measures
- Escrow funds isolated from campaigner wallet until approval
- Duplicate vote prevention (database unique constraint)
- Status-based access control (can't withdraw PENDING milestone)
- Transaction atomicity (all-or-nothing fund releases)

### 🎯 Business Logic
- Milestone-based funding ensures accountability
- Donor governance through voting
- Evidence requirement for transparency
- Rejection allows for improvement and resubmission

---

**Document Version**: 1.0  
**System Status**: ✅ Production Ready  
**Last Updated**: November 26, 2025
