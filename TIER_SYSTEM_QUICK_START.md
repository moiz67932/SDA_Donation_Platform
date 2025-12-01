# Subscription Tier System - Quick Start Guide

## Prerequisites
- MySQL 5.7+ running
- Database `fundraising_platform` created
- Java 11+ installed
- JavaFX configured
- Maven dependencies installed

## Quick Setup (5 minutes)

### Step 1: Database Setup

Run the tier setup script:

```bash
cd src/main/resources
mysql -u root -p fundraising_platform < campaign_tier_setup.sql
```

Or manually in MySQL Workbench:
1. Open `src/main/resources/campaign_tier_setup.sql`
2. Execute the script

**What this does:**
- Creates `subscription_tiers` table
- Creates `subscriptions` table
- Sets up proper indexes and foreign keys

### Step 2: Create Test Data (Optional)

Run the test script:

```bash
cd ../../../  # Back to project root
mysql -u root -p fundraising_platform < tier_system_test.sql
```

**What this does:**
- Creates a test campaign (if needed)
- Adds 4 sample tiers (Bronze $10, Silver $25, Gold $50, Platinum $100)
- Shows verification queries

### Step 3: Compile and Run

```bash
mvn clean install
mvn javafx:run
```

## Testing the System

### As a Campaigner:

1. **Login as Campaigner**
   - Email: Use existing campaigner account or create new one
   
2. **Create Campaign with Tiers**
   - Click "Create Campaign"
   - Fill in details:
     - Title: "My Awesome Project"
     - Category: Select any
     - Description: Write something
     - Goal: 5000
   - Click "Create Campaign"
   - When prompted, click "Add Tiers Now"

3. **Add Subscription Tiers**
   - In the tier dialog:
     - **Tier 1:**
       - Name: "Basic Supporter"
       - Amount: 10.00
       - Description: "Support our project monthly"
       - Benefits: "Monthly updates\nDiscord access"
     - Click "Add Tier"
     
     - **Tier 2:**
       - Name: "Premium Supporter"
       - Amount: 25.00
       - Description: "Enhanced support level"
       - Benefits: "All Basic benefits\nEarly access\nQ&A sessions"
     - Click "Add Tier"
   
   - Click "Close" when done

4. **Manage Tiers Later**
   - Go to "My Campaigns"
   - Select your campaign
   - Click "Manage Subscription Tiers"
   - You can now add/edit/delete tiers

### As a Donor:

1. **Login as Donor**
   - Email: Use existing donor account or create new one

2. **Browse and Subscribe**
   - Click "Browse Campaigns"
   - Select a campaign with tiers
   - Click "Subscribe"
   - Review available tiers
   - Select a tier (e.g., "Basic Supporter")
   - Review benefits in the right panel
   - Click "Subscribe"

3. **View Subscriptions**
   - Go to "My Subscriptions"
   - See your active subscriptions
   - View details: tier, amount, next billing date
   - Option to cancel if needed

## Verify Everything Works

### Check Database:

```sql
-- See all tiers
SELECT 
    c.title AS campaign,
    st.tier_name,
    st.monthly_amount,
    st.description
FROM subscription_tiers st
JOIN campaigns c ON st.campaign_id = c.id
ORDER BY c.id, st.monthly_amount;

-- See all subscriptions
SELECT 
    u.name AS donor,
    c.title AS campaign,
    st.tier_name AS tier,
    s.monthly_amount,
    s.status,
    s.next_billing_date
FROM subscriptions s
JOIN users u ON s.donor_id = u.id
JOIN campaigns c ON s.campaign_id = c.id
JOIN subscription_tiers st ON s.tier_id = st.id
WHERE s.status = 'ACTIVE';

-- Check campaign revenue
SELECT 
    c.title,
    c.collected_amount,
    COUNT(DISTINCT s.id) AS subscription_count,
    SUM(CASE WHEN s.status = 'ACTIVE' THEN s.monthly_amount ELSE 0 END) AS monthly_recurring
FROM campaigns c
LEFT JOIN subscriptions s ON c.id = s.campaign_id
GROUP BY c.id, c.title, c.collected_amount;
```

## Troubleshooting

### Problem: "No tiers available" when subscribing
**Solution:** 
- Campaign doesn't have tiers yet
- Login as campaigner → My Campaigns → Select campaign → Manage Subscription Tiers → Add tiers

### Problem: Can't delete a tier
**Solution:** 
- Tier has active subscriptions
- You can only delete tiers with no subscribers
- Cancel subscriptions first, or create new tiers instead

### Problem: Subscription dialog doesn't open
**Solution:** 
- Check console for errors
- Verify `subscription_dialog.fxml` exists in `src/main/resources/fxml/`
- Rebuild project: `mvn clean install`

### Problem: Database errors
**Solution:** 
- Run setup script again: `campaign_tier_setup.sql`
- Check if tables exist:
  ```sql
  SHOW TABLES LIKE 'subscription%';
  ```
- Verify foreign keys:
  ```sql
  SHOW CREATE TABLE subscription_tiers;
  SHOW CREATE TABLE subscriptions;
  ```

## Expected Results

After setup, you should have:

✅ **Campaigner Side:**
- Can create campaigns and immediately add tiers
- Can manage tiers from "My Campaigns"
- Can add/edit/delete tiers (with validation)
- See clear form with tier name, amount, description, benefits

✅ **Donor Side:**
- Can browse campaigns
- Can click "Subscribe" to see tiers
- Can select tier and view benefits
- Can complete subscription
- Can view/manage subscriptions in "My Subscriptions"

✅ **Database:**
- `subscription_tiers` table with proper structure
- `subscriptions` table with status tracking
- Foreign key relationships working
- Unique constraint on (campaign_id, tier_name)

## Next Steps

1. **Test all workflows** using the checklists in `TIER_SYSTEM_COMPLETE_GUIDE.md`
2. **Add more sample data** by running test script multiple times
3. **Customize tier names** to match your use case (e.g., "Backer", "Patron", "Champion")
4. **Monitor subscriptions** using the database queries provided

## Support

For detailed information, see:
- `TIER_SYSTEM_COMPLETE_GUIDE.md` - Complete documentation
- `tier_system_test.sql` - Test queries and verification
- `src/main/resources/campaign_tier_setup.sql` - Schema setup

## Summary

The tier system is fully working! You can now:
1. **Create campaigns** with subscription tiers
2. **Manage tiers** for each campaign
3. **Donors can subscribe** with recurring monthly payments
4. **Track subscriptions** through the UI and database
5. **Full validation** and business logic in place

Enjoy your fully functional subscription tier system! 🎉
