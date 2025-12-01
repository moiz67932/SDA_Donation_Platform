-- Fix Missing Columns Migration Script
-- This script safely adds missing columns to existing tables
-- Run this if you get "Unknown column" errors

USE fundraising_platform;

-- Add missing columns to campaigns table if they don't exist
SET @db_name = 'fundraising_platform';

-- Check and add is_escrow_enabled column
SET @col_exists = (SELECT COUNT(*) 
                   FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = @db_name
                   AND TABLE_NAME = 'campaigns' 
                   AND COLUMN_NAME = 'is_escrow_enabled');

SET @sql = IF(@col_exists = 0, 
              'ALTER TABLE campaigns ADD COLUMN is_escrow_enabled BOOLEAN DEFAULT FALSE AFTER is_civic',
              'SELECT ''Column is_escrow_enabled already exists'' AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add is_reward_eligible column
SET @col_exists = (SELECT COUNT(*) 
                   FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = @db_name
                   AND TABLE_NAME = 'campaigns' 
                   AND COLUMN_NAME = 'is_reward_eligible');

SET @sql = IF(@col_exists = 0, 
              'ALTER TABLE campaigns ADD COLUMN is_reward_eligible BOOLEAN DEFAULT FALSE AFTER is_escrow_enabled',
              'SELECT ''Column is_reward_eligible already exists'' AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add is_philanthropic column
SET @col_exists = (SELECT COUNT(*) 
                   FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = @db_name
                   AND TABLE_NAME = 'campaigns' 
                   AND COLUMN_NAME = 'is_philanthropic');

SET @sql = IF(@col_exists = 0, 
              'ALTER TABLE campaigns ADD COLUMN is_philanthropic BOOLEAN DEFAULT FALSE AFTER end_date',
              'SELECT ''Column is_philanthropic already exists'' AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add is_civic column
SET @col_exists = (SELECT COUNT(*) 
                   FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = @db_name
                   AND TABLE_NAME = 'campaigns' 
                   AND COLUMN_NAME = 'is_civic');

SET @sql = IF(@col_exists = 0, 
              'ALTER TABLE campaigns ADD COLUMN is_civic BOOLEAN DEFAULT FALSE AFTER is_philanthropic',
              'SELECT ''Column is_civic already exists'' AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update existing campaigns to have default values
UPDATE campaigns 
SET is_escrow_enabled = COALESCE(is_escrow_enabled, FALSE),
    is_reward_eligible = COALESCE(is_reward_eligible, FALSE),
    is_philanthropic = COALESCE(is_philanthropic, FALSE),
    is_civic = COALESCE(is_civic, FALSE)
WHERE id > 0;

-- Verify the columns were added
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fundraising_platform' 
  AND TABLE_NAME = 'campaigns'
  AND COLUMN_NAME IN ('is_escrow_enabled', 'is_reward_eligible', 'is_philanthropic', 'is_civic')
ORDER BY ORDINAL_POSITION;

SELECT 'Migration completed successfully! All missing columns have been added.' AS status;
