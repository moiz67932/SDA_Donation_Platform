# Subscription Feature Quick Start Guide

## For Campaigners

### Setting Up Subscription Tiers

1. **Login** to your campaigner account
2. Navigate to **"My Campaigns"**
3. Select your campaign from the list
4. Click **"Manage Subscription Tiers"** button
5. In the Manage Subscription Tiers window:

#### Adding a New Tier
- Enter **Tier Name** (e.g., "Bronze", "Silver", "Gold", "Supporter", "Patron")
- Enter **Monthly Amount** (e.g., 10.00, 25.00, 50.00)
- Enter **Description** (brief description of the tier)
- Enter **Benefits** (what subscribers get, e.g., "Monthly updates, Supporter badge, Early access to news")
- Click **"Add Tier"**

#### Editing a Tier
- Click on a tier in the table to select it
- Form will auto-populate with tier details
- Modify any fields
- Click **"Update"**

#### Deleting a Tier
- Select the tier you want to delete
- Click **"Delete"**
- Confirm deletion
- **Note**: Tiers with active subscriptions cannot be deleted

### Example Tiers

```
Tier Name: Bronze
Monthly Amount: 10.00
Description: Basic supporter tier
Benefits: Monthly email updates, Bronze badge on profile, Thank you mention

Tier Name: Silver  
Monthly Amount: 25.00
Description: Premium supporter tier
Benefits: Everything in Bronze + Quarterly video calls, Silver badge, Early news access

Tier Name: Gold
Monthly Amount: 50.00
Description: VIP supporter tier
Benefits: Everything in Silver + Monthly 1-on-1 meetings, Gold badge, Special recognition
```

---

## For Donors

### Subscribing to a Campaign

1. **Login** to your donor account
2. Navigate to **"Browse Campaigns"**
3. Search for or select a campaign you want to support
4. Click **"Subscribe"** button
5. In the subscription dialog:
   - View available tiers
   - Select a tier from the list
   - Read the benefits and monthly amount
   - Click **"Subscribe"**
6. You'll see a confirmation message
7. Your first payment will be processed immediately
8. Next payment: One month from today

### Managing Your Subscriptions

1. From donor dashboard, click **"My Subscriptions"**
2. View all your active and paused subscriptions
3. For each subscription, you can see:
   - Campaign name
   - Tier name
   - Monthly amount
   - Start date
   - Next billing date
   - Status

#### Pausing a Subscription
- Select subscription from table
- Click **"Pause Subscription"**
- Payments will stop
- You can resume later

#### Cancelling a Subscription
- Select subscription from table
- Click **"Cancel Subscription"**
- Confirm cancellation
- Subscription ends immediately
- **Note**: This action cannot be undone

---

## Database Schema Quick Reference

### subscription_tiers table
```
- id: Unique identifier
- campaign_id: Campaign this tier belongs to
- tier_name: Name of the tier (e.g., "Bronze")
- monthly_amount: Cost per month
- description: Brief description
- benefits: List of benefits
```

### subscriptions table
```
- id: Unique identifier
- campaign_id: Campaign being supported
- donor_id: Donor who subscribed
- tier_id: Subscription tier ID
- tier_name: Name of tier (denormalized)
- monthly_amount: Amount charged monthly
- status: ACTIVE, PAUSED, CANCELLED, EXPIRED
- start_date: When subscription started
- next_billing_date: Next payment date
- cancel_date: When cancelled (if applicable)
```

---

## API Reference (for developers)

### SubscriptionService Methods

#### Tier Management
```java
// Create tier
SubscriptionTier createTier(Long campaignId, String tierName, 
                           double monthlyAmount, String description, String benefits)

// Update tier
void updateTier(Long tierId, String tierName, double monthlyAmount, 
               String description, String benefits)

// Delete tier
void deleteTier(Long tierId)

// Get tiers for campaign
List<SubscriptionTier> getTiersByCampaign(Long campaignId)

// Get tier by ID
SubscriptionTier getTierById(Long tierId)
```

#### Subscription Management
```java
// Create subscription using tier
Subscription subscribeWithTier(Long campaignId, Long donorId, Long tierId)

// Cancel subscription
void cancelSubscription(Long subscriptionId)

// Pause subscription
void pauseSubscription(Long subscriptionId)

// Get donor subscriptions
List<Subscription> getDonorSubscriptions(Long donorId)

// Get active subscriptions only
List<Subscription> getActiveSubscriptions(Long donorId)

// Process monthly payment
void processSubscriptionPayment(Subscription subscription)
```

---

## Troubleshooting

### Common Issues

**Issue**: Can't see "Subscribe" button
- **Solution**: Make sure the campaign is ACTIVE status
- Check that you're logged in as a donor

**Issue**: "No tiers available" message
- **Solution**: Campaign owner needs to create subscription tiers first
- Go to My Campaigns → Select Campaign → Manage Subscription Tiers

**Issue**: Can't delete a tier
- **Solution**: Check if there are active subscriptions using that tier
- Tiers with active subscriptions cannot be deleted

**Issue**: "Already have active subscription" error
- **Solution**: You can only have one active subscription per campaign
- Cancel or pause existing subscription first

**Issue**: Subscription not showing in My Subscriptions
- **Solution**: Refresh the page
- Check that subscription was created successfully (look for confirmation message)

---

## Best Practices

### For Campaigners:
1. **Create 3-5 tiers** - Provides options without overwhelming donors
2. **Clear tier names** - Use recognizable names like Bronze, Silver, Gold OR descriptive names like "Supporter", "Patron", "Champion"
3. **Progressive benefits** - Higher tiers should include all benefits of lower tiers plus more
4. **Reasonable pricing** - Consider your target audience's capacity
5. **Update benefits** - Keep benefits fresh and valuable

### For Donors:
1. **Start with lower tier** - You can always upgrade later
2. **Review benefits** - Make sure the tier value matches your expectations
3. **Manage subscriptions regularly** - Check "My Subscriptions" monthly
4. **Pause vs Cancel** - Use pause if you're temporarily unable to contribute

---

## Support

For issues or questions:
- Check this guide first
- Review the full implementation document: SUBSCRIPTION_IMPLEMENTATION.md
- Check application logs for error details
- Contact system administrator

---

## Version History

- **v1.0.0** - Initial implementation
  - Subscription tier creation and management
  - Donor subscription functionality
  - My Subscriptions screen
  - Monthly billing cycle support
