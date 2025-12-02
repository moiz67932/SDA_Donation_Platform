-- Simple fix for subscriptions table - add tier_id column

-- Add tier_id column if missing
ALTER TABLE subscriptions 
ADD COLUMN IF NOT EXISTS tier_id BIGINT NOT NULL AFTER donor_id;

-- Add foreign key constraint
ALTER TABLE subscriptions 
ADD CONSTRAINT IF NOT EXISTS fk_subscriptions_tier 
FOREIGN KEY (tier_id) REFERENCES subscription_tiers(id) ON DELETE RESTRICT;

-- Add index
CREATE INDEX IF NOT EXISTS idx_tier ON subscriptions(tier_id);

-- Show result
DESCRIBE subscriptions;
