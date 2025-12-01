# ✅ Subscription Tier System - Verification Checklist

## Pre-Flight Check

Use this checklist to verify that the subscription tier system is fully operational.

---

## 📋 Database Setup Verification

### Step 1: Check Tables Exist
Run in MySQL:
```sql
USE fundraising_platform;
SHOW TABLES LIKE 'subscription%';
```

**Expected Result:**
```
subscription_tiers
subscriptions
```

- [ ] Both tables appear in results

### Step 2: Verify Table Structure
```sql
DESCRIBE subscription_tiers;
DESCRIBE subscriptions;
```

**Check for:**
- [ ] subscription_tiers has: id, campaign_id, tier_name, monthly_amount, description, benefits
- [ ] subscriptions has: id, campaign_id, donor_id, tier_id, tier_name, monthly_amount, status
- [ ] Foreign keys are present
- [ ] Indexes are created

### Step 3: Test Data (Optional)
```sql
-- Run tier_system_test.sql
source tier_system_test.sql;
```

- [ ] Test campaign created
- [ ] 4 sample tiers added (Bronze, Silver, Gold, Platinum)
- [ ] No errors in script execution

---

## 🏗️ Code Compilation Check

### Step 1: Clean Build
```bash
mvn clean install
```

**Verify:**
- [ ] BUILD SUCCESS message
- [ ] No compilation errors
- [ ] All tests pass (if any)

### Step 2: Check for Errors
In IDE (VS Code/IntelliJ):
- [ ] No red underlines in CreateCampaignController.java
- [ ] No errors in ManageSubscriptionTiersController.java
- [ ] No errors in SubscriptionDialogController.java
- [ ] All imports resolve correctly

---

## 🎯 Campaigner Side Testing

### Test 1: Create Campaign with Tiers

**Steps:**
1. Run application: `mvn javafx:run`
2. Login as campaigner (or register new campaigner)
3. Click "Create Campaign"
4. Fill in:
   - Title: "Test Campaign for Tiers"
   - Category: COMMUNITY
   - Description: "Testing tier functionality"
   - Goal: 5000
5. Click "Create Campaign"

**Verify:**
- [ ] Campaign created successfully
- [ ] Dialog appears: "Would you like to add subscription tiers now?"
- [ ] Two buttons: "Add Tiers Now" and "Skip for Now"

### Test 2: Add Tiers Immediately

**Steps:**
1. Click "Add Tiers Now" in the dialog
2. Tier Management dialog opens

**Verify:**
- [ ] Dialog title: "Add Subscription Tiers - [Campaign Name]"
- [ ] Top section shows empty tier table
- [ ] Bottom section shows form with fields: Name, Amount, Description, Benefits
- [ ] Buttons: Add, Update, Delete, Clear, Close

### Test 3: Create First Tier

**Steps:**
1. In tier form, enter:
   - Tier Name: "Bronze Supporter"
   - Monthly Amount: 10.00
   - Description: "Basic support level"
   - Benefits: "Monthly updates\nDiscord access\nName in credits"
2. Click "Add Tier"

**Verify:**
- [ ] Success message: "Subscription tier created successfully!"
- [ ] Tier appears in table above
- [ ] Form is cleared
- [ ] Table shows: Bronze Supporter | $10.00 | Basic support level

### Test 4: Create Multiple Tiers

**Steps:**
Repeat Test 3 with:
- Silver Supporter: $25, enhanced benefits
- Gold Supporter: $50, premium benefits

**Verify:**
- [ ] All 3 tiers appear in table
- [ ] Sorted by creation order
- [ ] No duplicate name errors

### Test 5: Edit a Tier

**Steps:**
1. Click on "Silver Supporter" in table
2. Form populates with tier data
3. Change amount to $30
4. Change description
5. Click "Update"

**Verify:**
- [ ] Tier data loads correctly in form
- [ ] Update succeeds
- [ ] Table reflects new amount ($30)
- [ ] Success message shown

### Test 6: Tier Validation

**Test 6a: Duplicate Name**
1. Try to add tier with name "Bronze Supporter"
   - [ ] Error: "A tier with this name already exists"

**Test 6b: Zero Amount**
1. Try to add tier with amount 0
   - [ ] Error: "Monthly amount must be greater than 0"

**Test 6c: Negative Amount**
1. Try to add tier with amount -10
   - [ ] Error: "Monthly amount must be greater than 0"

**Test 6d: Empty Name**
1. Try to add tier with empty name
   - [ ] Error: "Please enter a tier name"

### Test 7: Delete Tier (No Subscriptions)

**Steps:**
1. Select "Gold Supporter"
2. Click "Delete"
3. Confirm deletion

**Verify:**
- [ ] Confirmation dialog appears
- [ ] After confirmation, tier is removed from table
- [ ] Success message shown

### Test 8: Manage Tiers Later

**Steps:**
1. Close tier dialog (if open)
2. Go to "My Campaigns"
3. Select your test campaign
4. Click "Manage Subscription Tiers"

**Verify:**
- [ ] Tier management dialog opens
- [ ] Shows all existing tiers
- [ ] Can add/edit/delete as before

---

## 👥 Donor Side Testing

### Test 9: Browse and View Campaign

**Steps:**
1. Logout (or use different browser/incognito)
2. Login as donor (or register new donor)
3. Click "Browse Campaigns"
4. Find your test campaign in list

**Verify:**
- [ ] Campaign appears in browse list
- [ ] Shows title, category, goal, collected amount, status
- [ ] Subscribe button is visible

### Test 10: Open Subscription Dialog

**Steps:**
1. Select your test campaign
2. Click "Subscribe" button

**Verify:**
- [ ] Subscription dialog opens
- [ ] Title shows: "Subscribe to Campaign"
- [ ] Campaign name displayed at top
- [ ] Left panel shows list of tiers
- [ ] Right panel ready to show tier details

### Test 11: View Tier Details

**Steps:**
1. Click on "Bronze Supporter" in tier list

**Verify:**
- [ ] Right panel updates with tier details
- [ ] Shows monthly amount: $10.00
- [ ] Shows description
- [ ] Shows benefits (multi-line)

### Test 12: Select Different Tiers

**Steps:**
1. Click through each tier in the list

**Verify:**
- [ ] Each click updates right panel
- [ ] Correct amount displayed for each tier
- [ ] Correct benefits shown for each tier

### Test 13: Subscribe to a Tier

**Steps:**
1. Select "Bronze Supporter" ($10)
2. Review details
3. Click "Subscribe" button

**Verify:**
- [ ] Success message: "You have successfully subscribed..."
- [ ] Message includes tier name and amount
- [ ] Dialog closes
- [ ] Campaign list refreshes (if on browse page)

### Test 14: Check "My Subscriptions"

**Steps:**
1. Go to Donor Dashboard
2. Click "My Subscriptions"

**Verify:**
- [ ] Subscription appears in list
- [ ] Shows: Campaign name, tier name, amount, status
- [ ] Status is "ACTIVE"
- [ ] Next billing date is shown (1 month from today)

### Test 15: Try Duplicate Subscription

**Steps:**
1. Go back to Browse Campaigns
2. Try to subscribe to same campaign again

**Verify:**
- [ ] Error message: "You already have an active subscription to this campaign"
- [ ] Cannot create duplicate subscription

### Test 16: View Subscription Details

**Steps:**
1. In "My Subscriptions"
2. Select your subscription
3. View details panel

**Verify:**
- [ ] Shows all subscription information
- [ ] Campaign name correct
- [ ] Tier name correct
- [ ] Monthly amount correct
- [ ] Start date shown
- [ ] Next billing date shown
- [ ] Status is ACTIVE

### Test 17: Cancel Subscription

**Steps:**
1. Select subscription
2. Click "Cancel" button
3. Confirm cancellation

**Verify:**
- [ ] Confirmation dialog appears
- [ ] After confirmation, status changes to "CANCELLED"
- [ ] Cancel date is recorded
- [ ] Success message shown

---

## 🗄️ Database Verification

### Test 18: Verify Tier Records

```sql
SELECT 
    st.id,
    c.title AS campaign,
    st.tier_name,
    st.monthly_amount,
    st.description
FROM subscription_tiers st
JOIN campaigns c ON st.campaign_id = c.id
WHERE c.title LIKE '%Test Campaign%';
```

**Verify:**
- [ ] All created tiers appear
- [ ] Amounts are correct
- [ ] Descriptions are correct
- [ ] Campaign linkage is correct

### Test 19: Verify Subscription Records

```sql
SELECT 
    s.id,
    u.name AS donor,
    c.title AS campaign,
    st.tier_name,
    s.monthly_amount,
    s.status,
    s.start_date,
    s.next_billing_date
FROM subscriptions s
JOIN users u ON s.donor_id = u.id
JOIN campaigns c ON s.campaign_id = c.id
JOIN subscription_tiers st ON s.tier_id = st.id
WHERE c.title LIKE '%Test Campaign%';
```

**Verify:**
- [ ] Subscription record exists
- [ ] Donor linkage correct
- [ ] Campaign linkage correct
- [ ] Tier linkage correct
- [ ] Amount matches tier amount
- [ ] Status is ACTIVE (or CANCELLED if you tested that)
- [ ] Dates are reasonable

### Test 20: Check Campaign Updated Amount

```sql
SELECT 
    id,
    title,
    goal_amount,
    collected_amount
FROM campaigns
WHERE title LIKE '%Test Campaign%';
```

**Verify:**
- [ ] collected_amount increased by subscription amount
- [ ] If subscribed to $10 tier, collected_amount shows at least $10

### Test 21: Check Credit Transaction

```sql
SELECT 
    ct.id,
    u.name AS user,
    ct.credits_earned,
    ct.credits_spent,
    ct.description,
    ct.created_at
FROM credit_transactions ct
JOIN users u ON ct.user_id = u.id
WHERE u.role = 'DONOR'
ORDER BY ct.created_at DESC
LIMIT 5;
```

**Verify:**
- [ ] Credit transaction recorded for donor
- [ ] Credits earned = subscription_amount * 0.01
- [ ] Description mentions the campaign
- [ ] Timestamp is recent

---

## 🔧 Edge Cases & Error Handling

### Test 22: Delete Tier with Active Subscription

**Steps:**
1. Login as campaigner
2. Go to manage tiers for campaign with active subscription
3. Try to delete tier that has a subscription

**Verify:**
- [ ] Error message: "Cannot delete tier with active subscriptions"
- [ ] Tier remains in database
- [ ] No data corruption

### Test 23: Subscribe to Inactive Campaign

**Steps:**
1. Set a campaign status to PENDING_REVIEW in database
2. Try to subscribe to it as donor

**Verify:**
- [ ] Error or no subscribe button shown
- [ ] Cannot create subscription
- [ ] Appropriate message displayed

### Test 24: Large Amounts

**Steps:**
1. Create tier with amount 9999.99
2. Subscribe to it

**Verify:**
- [ ] Amount accepts large values
- [ ] Displays correctly in UI
- [ ] Stores correctly in database
- [ ] Credits calculated correctly

### Test 25: Special Characters in Benefits

**Steps:**
1. Create tier with benefits containing:
   - Line breaks
   - Bullet points (•)
   - Emojis (if supported)

**Verify:**
- [ ] All characters saved
- [ ] Display correctly in subscription dialog
- [ ] No encoding issues

---

## 📊 Performance & Load Testing

### Test 26: Multiple Tiers Performance

**Steps:**
1. Create campaign with 10 tiers
2. Open subscription dialog

**Verify:**
- [ ] Dialog loads quickly
- [ ] All tiers visible
- [ ] Scrolling works smoothly
- [ ] Selection is responsive

### Test 27: Many Subscriptions

**Steps:**
1. Create 5-10 subscriptions as different donors
2. Check "My Campaigns" as campaigner

**Verify:**
- [ ] Campaign collected_amount shows sum of all subscriptions
- [ ] Page loads within reasonable time
- [ ] Data is accurate

---

## 📱 UI/UX Verification

### Test 28: UI Responsiveness

**Check:**
- [ ] Tier management dialog resizes properly
- [ ] Table columns are properly sized
- [ ] Form fields are aligned
- [ ] Buttons are properly styled
- [ ] Text areas show scroll bars when needed

### Test 29: User Feedback

**Verify all messages appear:**
- [ ] Success messages (green/checkmark)
- [ ] Error messages (red/X)
- [ ] Warning messages (yellow/!)
- [ ] Confirmation dialogs before destructive actions

### Test 30: Navigation Flow

**Check:**
- [ ] Can navigate: Dashboard → Create Campaign → Tiers → Dashboard
- [ ] Can navigate: Dashboard → My Campaigns → Manage Tiers
- [ ] Can navigate: Dashboard → Browse → Subscribe → My Subscriptions
- [ ] Back buttons work correctly
- [ ] Dialog close buttons work

---

## 🎓 Final Integration Test

### Complete End-to-End Workflow

**Scenario:** New campaign with subscriptions

**Steps:**
1. Campaigner creates campaign "Ocean Cleanup 2025"
2. Adds 3 tiers: $10, $25, $50
3. Admin approves campaign (set status to ACTIVE in DB)
4. Donor 1 subscribes to $10 tier
5. Donor 2 subscribes to $25 tier
6. Donor 3 subscribes to $50 tier
7. Check campaign collected amount
8. Check each donor's credits
9. Check subscriptions table
10. Campaigner edits $25 tier to $30
11. Verify existing subscriptions still show $25 (not affected)

**All steps complete without errors?**
- [ ] Yes, system is fully functional!
- [ ] No, investigate failed steps

---

## 📋 Checklist Summary

### Core Functionality
- [ ] Database tables created and configured
- [ ] Code compiles without errors
- [ ] Campaigner can create tiers during campaign creation
- [ ] Campaigner can manage tiers from My Campaigns
- [ ] Donor can browse and subscribe to tiers
- [ ] Subscriptions appear in My Subscriptions
- [ ] Database records are accurate

### Validation & Security
- [ ] Tier name uniqueness enforced
- [ ] Amount validation works
- [ ] Cannot delete tiers with subscriptions
- [ ] Duplicate subscription prevention
- [ ] Authorization checks in place

### Business Logic
- [ ] Credits awarded correctly (1 per $100)
- [ ] Campaign amount updated
- [ ] Next billing date calculated
- [ ] Notifications sent (if implemented)
- [ ] Escrow support (if enabled)

### UI/UX
- [ ] All dialogs open correctly
- [ ] Forms are user-friendly
- [ ] Error messages are clear
- [ ] Success feedback provided
- [ ] Navigation is intuitive

---

## 🎉 Final Verdict

**Total Tests:** 30  
**Tests Passed:** _____  
**Tests Failed:** _____  

### If all tests pass:
✅ **SYSTEM IS PRODUCTION READY!**

The subscription tier system is fully functional and ready for use!

### If some tests fail:
🔧 **TROUBLESHOOTING NEEDED**

1. Review failed test details
2. Check console logs for errors
3. Verify database connections
4. Ensure all files are saved and compiled
5. Consult TIER_SYSTEM_COMPLETE_GUIDE.md for help

---

## 📞 Support Resources

- **Complete Guide:** TIER_SYSTEM_COMPLETE_GUIDE.md
- **Quick Start:** TIER_SYSTEM_QUICK_START.md
- **Visual Flow:** TIER_SYSTEM_VISUAL_FLOW.md
- **Implementation Summary:** TIER_SYSTEM_IMPLEMENTATION_SUMMARY.md
- **Test SQL:** tier_system_test.sql
- **Setup SQL:** src/main/resources/campaign_tier_setup.sql

---

*Checklist Version: 1.0*  
*Last Updated: November 26, 2025*  
*Project: CrowdAid Fundraising Platform*
