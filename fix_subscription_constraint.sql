-- Fix subscription unique constraint to only apply to ACTIVE subscriptions
-- This allows a user to have multiple cancelled/paused subscriptions for the same campaign
-- but only one ACTIVE subscription

-- Drop the existing constraint
ALTER TABLE subscriptions DROP INDEX unique_donor_campaign;

-- Add a new unique constraint only for ACTIVE subscriptions
-- MySQL doesn't support filtered unique indexes directly, so we use a unique index with a WHERE clause workaround
-- For MySQL 5.7+, we can use a trigger or stored generated column

-- Alternative: Create a functional index that includes NULL for non-ACTIVE subscriptions
-- This effectively makes the constraint only apply to ACTIVE rows
ALTER TABLE subscriptions ADD UNIQUE INDEX unique_active_subscription (donor_id, campaign_id, 
    (CASE WHEN status = 'ACTIVE' THEN 'ACTIVE' ELSE NULL END));

-- If the above doesn't work in your MySQL version, use this approach:
-- DROP the index above and create a standard one, relying on application logic
-- ALTER TABLE subscriptions DROP INDEX unique_active_subscription;
-- ALTER TABLE subscriptions ADD INDEX idx_donor_campaign_status (donor_id, campaign_id, status);
