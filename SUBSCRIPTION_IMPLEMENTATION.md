# Subscription Functionality Implementation Summary

## Overview
Complete implementation of subscription-based donations for the CrowdAid platform, allowing donors to subscribe to campaigns with different tiers (Bronze, Silver, Gold, etc.) and enabling campaigners to create and manage custom subscription tiers.

## Database Schema Changes

### New Table: `subscription_tiers`
```sql
CREATE TABLE subscription_tiers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    tier_name VARCHAR(100) NOT NULL,
    monthly_amount DECIMAL(15, 2) NOT NULL,
    description TEXT,
    benefits TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    UNIQUE KEY unique_campaign_tier (campaign_id, tier_name)
)
```

### Updated Table: `subscriptions`
```sql
CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    tier_id BIGINT NOT NULL,
    tier_name VARCHAR(100) NOT NULL,
    monthly_amount DECIMAL(15, 2) NOT NULL,
    status ENUM('ACTIVE', 'PAUSED', 'CANCELLED', 'EXPIRED') DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    next_billing_date DATE NOT NULL,
    cancel_date DATE NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (tier_id) REFERENCES subscription_tiers(id) ON DELETE CASCADE,
    UNIQUE KEY unique_donor_campaign (donor_id, campaign_id, status)
)
```

## Model Classes

### 1. SubscriptionTier (Enhanced)
**Location**: `src/main/java/com/crowdaid/model/donation/SubscriptionTier.java`

**Key Features**:
- Extends `BaseEntity` for ID and timestamp support
- Contains `campaignId`, `tierName`, `monthlyAmount`, `description`, and `benefits`
- Provides `getDisplayText()` for formatted display
- Backward compatible with legacy code

### 2. Subscription (Enhanced)
**Location**: `src/main/java/com/crowdaid/model/donation/Subscription.java`

**Key Features**:
- Added `tierId` field linking to subscription tiers
- Added `startDate`, `nextBillingDate`, and `cancelDate` for billing cycle tracking
- Methods: `pause()`, `resume()`, `cancel()`
- Auto-initializes next billing date as one month from start date

## Repository Layer

### 1. SubscriptionTierRepository Interface
**Location**: `src/main/java/com/crowdaid/repository/interfaces/SubscriptionTierRepository.java`

**Methods**:
- `findById(Long id)` - Find tier by ID
- `findByCampaign(Long campaignId)` - Get all tiers for a campaign
- `findByCampaignAndName(Long campaignId, String tierName)` - Find specific tier
- `save(SubscriptionTier tier)` - Create new tier
- `update(SubscriptionTier tier)` - Update existing tier
- `delete(Long id)` - Delete tier
- `countActiveSubscriptions(Long tierId)` - Count active subscriptions using tier

### 2. MySQLSubscriptionTierRepository
**Location**: `src/main/java/com/crowdaid/repository/mysql/MySQLSubscriptionTierRepository.java`

Complete MySQL implementation with proper error handling and logging.

### 3. MySQLSubscriptionRepository (Updated)
**Location**: `src/main/java/com/crowdaid/repository/mysql/MySQLSubscriptionRepository.java`

**Updates**:
- Modified queries to JOIN with `subscription_tiers` table
- Added support for `tier_id` field
- Enhanced `mapResultSetToSubscription()` to include tier information
- Proper handling of dates and timestamps

## Service Layer

### SubscriptionService (Enhanced)
**Location**: `src/main/java/com/crowdaid/service/SubscriptionService.java`

**New Tier Management Methods**:

1. **`createTier(campaignId, tierName, monthlyAmount, description, benefits)`**
   - Creates new subscription tier for a campaign
   - Validates campaign exists
   - Checks for duplicate tier names
   
2. **`updateTier(tierId, tierName, monthlyAmount, description, benefits)`**
   - Updates existing tier
   - Validates tier exists
   
3. **`deleteTier(tierId)`**
   - Deletes tier if no active subscriptions
   - Prevents deletion of tiers in use
   
4. **`getTiersByCampaign(campaignId)`**
   - Returns list of all tiers for a campaign
   - Sorted by monthly amount
   
5. **`getTierById(tierId)`**
   - Retrieves specific tier by ID
   
6. **`subscribeWithTier(campaignId, donorId, tierId)`**
   - Creates subscription using tier ID
   - Validates campaign is active
   - Prevents duplicate subscriptions
   - Processes initial payment
   - Sends notifications

**Existing Methods** (work with new tier system):
- `subscribe()` - Legacy method for backward compatibility
- `cancelSubscription()` - Cancel active subscription
- `pauseSubscription()` - Pause subscription temporarily
- `processSubscriptionPayment()` - Handle monthly billing
- `getDonorSubscriptions()` - Get donor's subscriptions
- `getActiveSubscriptions()` - Get active subscriptions only

## Controller Layer

### 1. SubscriptionDialogController (Rewritten)
**Location**: `src/main/java/com/crowdaid/controller/SubscriptionDialogController.java`

**Features**:
- Loads subscription tiers from database dynamically
- Displays tier information in ListView
- Shows tier benefits and monthly amount
- Validates user is logged in
- Creates subscription via `subscribeWithTier()` method
- Provides success/error feedback

**FXML**: `src/main/resources/fxml/subscription_dialog.fxml`

### 2. BrowseCampaignsController (Enhanced)
**Location**: `src/main/java/com/crowdaid/controller/BrowseCampaignsController.java`

**New Features**:
- Added "Subscribe" button to campaign browsing
- Opens subscription dialog with selected campaign
- Reloads campaigns after subscription

**FXML Update**: `src/main/resources/fxml/browse_campaigns.fxml`
- Added subscribe button to action buttons section

### 3. MySubscriptionsController (Enhanced)
**Location**: `src/main/java/com/crowdaid/controller/MySubscriptionsController.java`

**Features**:
- Displays all donor subscriptions with tier information
- Shows tier name, monthly amount, start date, next billing date, status
- Allows pausing and cancelling subscriptions
- Custom cell factories for proper data display

**FXML**: Already exists at `src/main/resources/fxml/my_subscriptions.fxml`

### 4. ManageSubscriptionTiersController (NEW)
**Location**: `src/main/java/com/crowdaid/controller/ManageSubscriptionTiersController.java`

**Features**:
- Campaigner interface for managing subscription tiers
- CRUD operations for tiers:
  - **Create**: Add new tier with name, amount, description, benefits
  - **Read**: View all tiers in table
  - **Update**: Modify selected tier
  - **Delete**: Remove tier (if no active subscriptions)
- Form validation for all fields
- Selection-based editing
- Clear form functionality

**FXML**: `src/main/resources/fxml/manage_subscription_tiers.fxml`
- Split pane layout
- Top: Table showing existing tiers
- Bottom: Form for adding/editing tiers
- Action buttons: Add, Update, Delete, Clear, Close

### 5. MyCampaignsController (Enhanced)
**Location**: `src/main/java/com/crowdaid/controller/MyCampaignsController.java`

**New Features**:
- Added "Manage Subscription Tiers" button
- Opens tier management dialog for selected campaign
- Modal dialog prevents multiple windows

**FXML Update**: `src/main/resources/fxml/my_campaigns.fxml`
- Added manage subscription tiers button

## User Workflows

### Donor Workflow - Subscribing to Campaign

1. **Browse Campaigns**
   - Navigate to "Browse Campaigns" from donor dashboard
   - Search/filter campaigns
   - Select a campaign

2. **View Subscription Tiers**
   - Click "Subscribe" button
   - Subscription dialog opens showing available tiers
   - View tier details:
     - Tier name (Bronze, Silver, Gold, etc.)
     - Monthly amount
     - Description
     - Benefits list

3. **Subscribe**
   - Select desired tier from list
   - Click "Subscribe" button
   - System validates:
     - User is logged in
     - Campaign is active
     - No duplicate subscription exists
   - Initial payment is processed
   - Confirmation message displayed
   - Notifications sent to donor and campaigner

4. **Manage Subscriptions**
   - Navigate to "My Subscriptions" from donor dashboard
   - View all active/paused subscriptions
   - See subscription details:
     - Campaign name
     - Tier name
     - Monthly amount
     - Start date
     - Next billing date
     - Status
   - Actions available:
     - **Pause**: Temporarily stop payments
     - **Cancel**: Permanently end subscription

### Campaigner Workflow - Managing Subscription Tiers

1. **Access Tier Management**
   - Navigate to "My Campaigns" from campaigner dashboard
   - Select a campaign
   - Click "Manage Subscription Tiers" button
   - Tier management dialog opens

2. **Create New Tier**
   - Enter tier details:
     - **Tier Name**: e.g., "Bronze", "Silver", "Gold", "Platinum"
     - **Monthly Amount**: e.g., 10.00, 25.00, 50.00
     - **Description**: Brief description of tier
     - **Benefits**: List of benefits subscribers receive
   - Click "Add Tier"
   - Tier is saved to database
   - Table refreshes to show new tier

3. **Edit Existing Tier**
   - Select tier from table
   - Form auto-populates with tier data
   - Modify fields as needed
   - Click "Update" button
   - Changes are saved

4. **Delete Tier**
   - Select tier from table
   - Click "Delete" button
   - Confirm deletion
   - System checks:
     - If active subscriptions exist, deletion is prevented
     - Otherwise, tier is removed
   - Table refreshes

5. **View Tier Usage**
   - Table shows all tiers with:
     - Tier name
     - Monthly amount
     - Description
   - Can see which tiers are popular based on subscription counts

## Subscription Billing Cycle

### Current Implementation
- **Start Date**: Set when subscription is created
- **Next Billing Date**: Automatically set to one month from start date
- **Status**: ACTIVE, PAUSED, CANCELLED, EXPIRED

### Monthly Payment Processing
The `processSubscriptionPayment()` method in `SubscriptionService` handles:

1. **Payment Processing**:
   - Generates unique transaction reference
   - Adds funds to campaign (escrow if enabled)
   - Updates campaign collected amount
   - Awards credits to donor (1% of donation)
   - Logs transaction

2. **Billing Cycle**:
   - Subscriptions have `next_billing_date` field
   - System can run a scheduled job to:
     - Query subscriptions where `next_billing_date <= TODAY`
     - Process payment for each
     - Update `next_billing_date` to one month later
   - Failed payments can be retried or subscription marked as EXPIRED

### Recommended Scheduled Job (Future Enhancement)
```java
// Pseudo-code for scheduled billing processor
@Scheduled(cron = "0 0 1 * * ?") // Run daily at 1 AM
public void processMonthlyBillings() {
    List<Subscription> dueSubscriptions = 
        subscriptionRepository.findDueForBilling(LocalDate.now());
    
    for (Subscription sub : dueSubscriptions) {
        try {
            subscriptionService.processSubscriptionPayment(sub);
            sub.setNextBillingDate(sub.getNextBillingDate().plusMonths(1));
            subscriptionRepository.update(sub);
        } catch (Exception e) {
            // Handle payment failure
            // Maybe send notification, retry, or mark as failed
        }
    }
}
```

## Key Features Implemented

### For Donors:
✅ Browse campaigns and view available subscription tiers
✅ Subscribe to campaigns with chosen tier
✅ View all active subscriptions
✅ See subscription details (tier, amount, next billing date)
✅ Pause subscriptions temporarily
✅ Cancel subscriptions permanently
✅ Receive notifications on subscription creation
✅ Earn credits from subscription payments (1% rate)

### For Campaigners:
✅ Create multiple subscription tiers per campaign
✅ Set custom tier names (Bronze, Silver, Gold, Custom names)
✅ Define monthly amounts for each tier
✅ Add descriptions and benefits for tiers
✅ Edit existing tiers
✅ Delete tiers (if no active subscriptions)
✅ View all tiers for their campaigns
✅ Receive notifications when donors subscribe

### System Features:
✅ Database schema with proper foreign keys and constraints
✅ Unique constraint preventing duplicate active subscriptions
✅ Unique constraint preventing duplicate tier names per campaign
✅ Proper error handling and validation
✅ Logging for all operations
✅ Transaction recording for all payments
✅ Integration with escrow system (if campaign uses escrow)
✅ Integration with credit/reward system
✅ Notification system for all parties

## Files Created/Modified

### Created Files:
1. `src/main/java/com/crowdaid/repository/interfaces/SubscriptionTierRepository.java`
2. `src/main/java/com/crowdaid/repository/mysql/MySQLSubscriptionTierRepository.java`
3. `src/main/java/com/crowdaid/controller/ManageSubscriptionTiersController.java`
4. `src/main/resources/fxml/manage_subscription_tiers.fxml`

### Modified Files:
1. `src/main/resources/schema.sql` - Added subscription_tiers table, updated subscriptions table
2. `src/main/java/com/crowdaid/model/donation/SubscriptionTier.java` - Enhanced with BaseEntity, campaignId
3. `src/main/java/com/crowdaid/model/donation/Subscription.java` - Added tierId, dates, methods
4. `src/main/java/com/crowdaid/repository/mysql/MySQLSubscriptionRepository.java` - Updated queries
5. `src/main/java/com/crowdaid/service/SubscriptionService.java` - Added tier management methods
6. `src/main/java/com/crowdaid/controller/SubscriptionDialogController.java` - Complete rewrite
7. `src/main/java/com/crowdaid/controller/BrowseCampaignsController.java` - Added subscribe button
8. `src/main/java/com/crowdaid/controller/MySubscriptionsController.java` - Enhanced display
9. `src/main/java/com/crowdaid/controller/MyCampaignsController.java` - Added tier management
10. `src/main/resources/fxml/browse_campaigns.fxml` - Added subscribe button
11. `src/main/resources/fxml/my_campaigns.fxml` - Added tier management button

## Testing Recommendations

### Manual Testing Steps:

1. **Database Setup**:
   - Run the updated schema.sql to create/update tables
   - Verify subscription_tiers and subscriptions tables exist
   - Check constraints and foreign keys

2. **Campaigner Flow**:
   - Login as campaigner
   - Create a new campaign (or use existing)
   - Navigate to "My Campaigns"
   - Select campaign and click "Manage Subscription Tiers"
   - Create 3 tiers: Bronze ($10), Silver ($25), Gold ($50)
   - Edit a tier to change amount/benefits
   - Try to delete a tier
   - Verify all operations work correctly

3. **Donor Flow**:
   - Login as donor
   - Navigate to "Browse Campaigns"
   - Select a campaign with tiers
   - Click "Subscribe" button
   - Verify tiers are displayed correctly
   - Subscribe to a tier
   - Verify success message
   - Navigate to "My Subscriptions"
   - Verify subscription is listed
   - Try to pause the subscription
   - Try to cancel the subscription

4. **Edge Cases**:
   - Try to subscribe twice to same campaign (should fail)
   - Try to delete tier with active subscriptions (should fail)
   - Try to create tier with duplicate name (should fail)
   - Try to subscribe to campaign without tiers (should show warning)

## Future Enhancements

1. **Automated Billing**:
   - Implement scheduled job to process monthly payments
   - Add retry logic for failed payments
   - Implement payment method storage

2. **Subscription Analytics**:
   - Dashboard showing subscription metrics
   - Revenue forecasting
   - Tier popularity analysis
   - Churn rate tracking

3. **Advanced Features**:
   - Proration for mid-month tier changes
   - Annual subscription options with discount
   - Trial periods for new subscribers
   - Referral bonuses
   - Subscription gifting

4. **Notifications**:
   - Email reminders before billing
   - Payment failure notifications
   - Subscription renewal confirmations
   - Tier change confirmations

5. **Donor Portal Enhancements**:
   - Subscription history with payment records
   - Download invoices/receipts
   - Update payment method
   - Upgrade/downgrade tier options

## Conclusion

The subscription functionality has been successfully implemented with full CRUD capabilities for both donors and campaigners. The system supports:

- Multiple subscription tiers per campaign
- Custom tier names, amounts, and benefits
- Monthly recurring donations
- Subscription lifecycle management (active, paused, cancelled)
- Integration with existing payment, escrow, and reward systems
- Comprehensive UI for all user types
- Proper database design with referential integrity
- Complete error handling and validation

The implementation follows GRASP principles and maintains consistency with the existing codebase architecture.
