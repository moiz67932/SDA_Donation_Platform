-- Fix subscriptions table by adding tier_id column if missing

-- Select the database
USE fundraising_platform;

-- Check and add tier_id column if it doesn't exist
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'fundraising_platform'
    AND TABLE_NAME = 'subscriptions' 
    AND COLUMN_NAME = 'tier_id'
);

-- Add tier_id column if it doesn't exist
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE subscriptions ADD COLUMN tier_id BIGINT NOT NULL AFTER donor_id',
    'SELECT "Column tier_id already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key constraint if it doesn't exist
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = 'fundraising_platform'
    AND TABLE_NAME = 'subscriptions' 
    AND CONSTRAINT_NAME = 'subscriptions_ibfk_3'
);

SET @sql_fk = IF(@fk_exists = 0,
    'ALTER TABLE subscriptions ADD CONSTRAINT subscriptions_ibfk_3 FOREIGN KEY (tier_id) REFERENCES subscription_tiers(id) ON DELETE RESTRICT',
    'SELECT "Foreign key already exists" AS message'
);

PREPARE stmt_fk FROM @sql_fk;
EXECUTE stmt_fk;
DEALLOCATE PREPARE stmt_fk;

-- Add index on tier_id if it doesn't exist
SET @idx_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = 'fundraising_platform'
    AND TABLE_NAME = 'subscriptions' 
    AND INDEX_NAME = 'idx_tier'
);

SET @sql_idx = IF(@idx_exists = 0,
    'ALTER TABLE subscriptions ADD INDEX idx_tier (tier_id)',
    'SELECT "Index idx_tier already exists" AS message'
);

PREPARE stmt_idx FROM @sql_idx;
EXECUTE stmt_idx;
DEALLOCATE PREPARE stmt_idx;

-- Verify the changes
DESCRIBE subscriptions;
