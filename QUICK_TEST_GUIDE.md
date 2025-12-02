# Quick Testing Guide - Fixed Issues

## Before Testing
Run this SQL script to diagnose/fix any database issues:
```bash
mysql -u your_username -p your_database < debug_subscriptions.sql
```

## Test 1: Fix Subscription Display Issue ✅

### Problem
- "My Subscriptions" showed "No content in table"
- Error: "Failed to load subscriptions: Failed to retrieve subscriptions"
- But duplicate error when trying to subscribe again proved subscription exists

### How to Test
1. Start application: `mvn javafx:run`
2. Login as donor (Moiz)
3. Click "My Subscriptions" from dashboard
4. **✅ PASS:** Table loads and shows your subscription(s)
5. **❌ FAIL:** Still shows error - run debug_subscriptions.sql queries

## Test 2: Fix Reward Redemption Error ✅

### Problem
- Credits were deducted successfully
- But error dialog appeared anyway
- Should only show success message

### How to Test
1. Go to "Reward Shop" from dashboard
2. Select a reward you can afford
3. Click "Redeem"
4. Confirm the redemption
5. **✅ PASS:** Only success message appears, no error
6. **✅ PASS:** Credits deducted correctly
7. **❌ FAIL:** Error appears - check console logs

## Common Issues & Solutions

### Issue: Subscriptions still not showing
**Solution:**
```sql
-- Check if subscriptions exist
SELECT * FROM subscriptions WHERE donor_id = YOUR_USER_ID;

-- Fix missing tier names
UPDATE subscriptions s
LEFT JOIN subscription_tiers st ON s.tier_id = st.id
SET s.tier_name = st.tier_name
WHERE s.tier_name IS NULL OR s.tier_name = '';
```

### Issue: "Subscription tier not found" error
**Solution:**
1. Make sure subscription_tiers table has data
2. Verify tier_id in subscriptions table matches existing tier
```sql
SELECT * FROM subscription_tiers WHERE campaign_id = YOUR_CAMPAIGN_ID;
```

### Issue: Duplicate subscription check not working
**Current behavior (CORRECT):**
- Can have ONE active subscription per campaign
- Can have multiple CANCELLED or PAUSED subscriptions

**Unique constraint:** `(donor_id, campaign_id, status)`

## Verify Fixes Work

### Test Script
```bash
# 1. Compile
mvn clean compile

# 2. Check compilation success
# Should see: BUILD SUCCESS

# 3. Run application
mvn javafx:run

# 4. Test both issues mentioned above
```

## What Changed

### Code Changes
- `MySubscriptionsController.java` - Better exception handling
- `SubscriptionService.java` - Fixed duplicate tier retrieval
- `RewardShopController.java` - Atomic success handling
- `MySQLSubscriptionRepository.java` - Null-safe tier name handling

### Build Status
✅ Compiles successfully
✅ No syntax errors
✅ All imports resolved

## If Still Having Issues

1. **Check console output** when error occurs
2. **Run SQL diagnostics:**
   ```bash
   mysql -u root -p crowdaid < debug_subscriptions.sql > output.txt
   ```
3. **Check for null data:**
   - tier_name should not be NULL
   - status should not be NULL
   - tier_id should reference valid tier
4. **Verify user role:** Must be logged in as DONOR
5. **Check logs:** Look in console for stack traces

## Expected Results After Fix

| Action | Before | After |
|--------|--------|-------|
| View My Subscriptions | Error message | ✅ Shows subscriptions |
| Redeem Reward | Success + Error | ✅ Only success message |
| Subscribe to campaign | Works | ✅ Still works |
| Duplicate subscription | Error | ✅ Still prevents |

## Quick Database Check

```sql
-- See all subscriptions for current user
SELECT 
    s.id,
    c.title as campaign,
    s.tier_name,
    s.monthly_amount,
    s.status,
    s.start_date
FROM subscriptions s
JOIN campaigns c ON s.campaign_id = c.id
WHERE s.donor_id = (SELECT id FROM users WHERE name LIKE '%Moiz%')
ORDER BY s.created_at DESC;
```

Expected: Should return at least 1 row if you have a subscription
