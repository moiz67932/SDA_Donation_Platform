-- Migration script to add escrow_enabled column to campaigns table
-- Run this after initial schema setup

USE fundraising_platform;

-- Add escrow_enabled column to campaigns table (only if it doesn't exist)
SET @col_exists = (SELECT COUNT(*) 
                   FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'fundraising_platform' 
                   AND TABLE_NAME = 'campaigns' 
                   AND COLUMN_NAME = 'escrow_enabled');

SET @sql = IF(@col_exists = 0, 
              'ALTER TABLE campaigns ADD COLUMN escrow_enabled BOOLEAN DEFAULT FALSE AFTER image_url',
              'SELECT ''Column escrow_enabled already exists'' AS status');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update existing campaigns to have escrow disabled by default
UPDATE campaigns SET escrow_enabled = FALSE WHERE escrow_enabled IS NULL AND id > 0;

-- Add index for faster queries (only if it doesn't exist)
SET @index_exists = (SELECT COUNT(*) 
                     FROM INFORMATION_SCHEMA.STATISTICS 
                     WHERE TABLE_SCHEMA = 'fundraising_platform' 
                     AND TABLE_NAME = 'campaigns' 
                     AND INDEX_NAME = 'idx_escrow_enabled');

SET @sql_index = IF(@index_exists = 0,
                    'CREATE INDEX idx_escrow_enabled ON campaigns(escrow_enabled)',
                    'SELECT ''Index idx_escrow_enabled already exists'' AS status');

PREPARE stmt_index FROM @sql_index;
EXECUTE stmt_index;
DEALLOCATE PREPARE stmt_index;

SELECT 'Migration completed: escrow_enabled column verified' AS status;
