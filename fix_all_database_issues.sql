-- Fix all database schema issues for subscriptions and redemptions tables
-- Run this script in MySQL to fix both missing columns

USE fundraising_platform;

-- ==========================================
-- FIX 1: Add tier_id to subscriptions table
-- ==========================================

-- Check if tier_id column exists
SET @tier_id_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'fundraising_platform'
    AND TABLE_NAME = 'subscriptions' 
    AND COLUMN_NAME = 'tier_id'
);

-- Add tier_id column if it doesn't exist
SET @sql_tier_id = IF(@tier_id_exists = 0,
    'ALTER TABLE subscriptions ADD COLUMN tier_id BIGINT NOT NULL AFTER donor_id',
    'SELECT "tier_id column already exists" AS message'
);

PREPARE stmt1 FROM @sql_tier_id;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- Add foreign key for tier_id if not exists
SET @fk_tier_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = 'fundraising_platform'
    AND TABLE_NAME = 'subscriptions' 
    AND CONSTRAINT_NAME = 'fk_subscriptions_tier'
);

SET @sql_fk_tier = IF(@fk_tier_exists = 0 AND @tier_id_exists = 0,
    'ALTER TABLE subscriptions ADD CONSTRAINT fk_subscriptions_tier FOREIGN KEY (tier_id) REFERENCES subscription_tiers(id) ON DELETE RESTRICT',
    'SELECT "Foreign key for tier_id already exists or not needed" AS message'
);

PREPARE stmt2 FROM @sql_fk_tier;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Add index for tier_id if not exists
SET @idx_tier_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = 'fundraising_platform'
    AND TABLE_NAME = 'subscriptions' 
    AND INDEX_NAME = 'idx_tier'
);

SET @sql_idx_tier = IF(@idx_tier_exists = 0 AND @tier_id_exists = 0,
    'CREATE INDEX idx_tier ON subscriptions(tier_id)',
    'SELECT "Index idx_tier already exists or not needed" AS message'
);

PREPARE stmt3 FROM @sql_idx_tier;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- ==========================================
-- FIX 2: Add credits_used to redemptions table
-- ==========================================

-- Check if credits_used column exists
SET @credits_used_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'fundraising_platform'
    AND TABLE_NAME = 'redemptions' 
    AND COLUMN_NAME = 'credits_used'
);

-- Add credits_used column if it doesn't exist (as alias to credits_spent)
SET @sql_credits_used = IF(@credits_used_exists = 0,
    'ALTER TABLE redemptions ADD COLUMN credits_used DECIMAL(10, 2) NOT NULL AFTER donor_id',
    'SELECT "credits_used column already exists" AS message'
);

PREPARE stmt4 FROM @sql_credits_used;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

-- Check if redemption_date column exists
SET @redemption_date_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'fundraising_platform'
    AND TABLE_NAME = 'redemptions' 
    AND COLUMN_NAME = 'redemption_date'
);

-- Add redemption_date column if it doesn't exist
SET @sql_redemption_date = IF(@redemption_date_exists = 0,
    'ALTER TABLE redemptions ADD COLUMN redemption_date TIMESTAMP NULL AFTER status',
    'SELECT "redemption_date column already exists" AS message'
);

PREPARE stmt5 FROM @sql_redemption_date;
EXECUTE stmt5;
DEALLOCATE PREPARE stmt5;

-- ==========================================
-- Verification
-- ==========================================

SELECT '=== SUBSCRIPTIONS TABLE STRUCTURE ===' AS '';
DESCRIBE subscriptions;

SELECT '=== REDEMPTIONS TABLE STRUCTURE ===' AS '';
DESCRIBE redemptions;

SELECT 'Database fixes completed successfully!' AS message;
