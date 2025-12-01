-- Database Migration Fix for CrowdAid Platform
-- Run this script to fix missing tables and columns
-- Date: 2025-11-26

USE fundraising_platform;

-- =====================================================
-- FIX 1: Add credit_balance column to users table if missing
-- =====================================================
SET @dbname = DATABASE();
SET @tablename = 'users';
SET @columnname = 'credit_balance';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT ''Column credit_balance already exists in users table'' AS message;',
  'ALTER TABLE users ADD COLUMN credit_balance DECIMAL(10, 2) DEFAULT 0.00 AFTER total_withdrawn;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- FIX 2: Create payment_gateways table if missing
-- =====================================================
CREATE TABLE IF NOT EXISTS payment_gateways (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================
-- FIX 3: Insert default payment gateway if table is empty
-- =====================================================
INSERT INTO payment_gateways (name, is_active) 
SELECT 'Simulated Payment Gateway', TRUE
WHERE NOT EXISTS (SELECT 1 FROM payment_gateways WHERE name = 'Simulated Payment Gateway');

-- =====================================================
-- FIX 4: Verify escrow_accounts table has total_amount column
-- =====================================================
SET @tablename = 'escrow_accounts';
SET @columnname = 'total_amount';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT ''Column total_amount already exists in escrow_accounts table'' AS message;',
  'ALTER TABLE escrow_accounts ADD COLUMN total_amount DECIMAL(15, 2) DEFAULT 0.00 AFTER campaign_id;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- FIX 5: Verify escrow_accounts table has available_amount column
-- =====================================================
SET @columnname = 'available_amount';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT ''Column available_amount already exists in escrow_accounts table'' AS message;',
  'ALTER TABLE escrow_accounts ADD COLUMN available_amount DECIMAL(15, 2) DEFAULT 0.00 AFTER total_amount;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- FIX 6: Verify escrow_accounts table has released_amount column
-- =====================================================
SET @columnname = 'released_amount';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT ''Column released_amount already exists in escrow_accounts table'' AS message;',
  'ALTER TABLE escrow_accounts ADD COLUMN released_amount DECIMAL(15, 2) DEFAULT 0.00 AFTER available_amount;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- FIX 7: Update existing users to have 0 credit balance if NULL
-- =====================================================
UPDATE users SET credit_balance = 0.00 WHERE credit_balance IS NULL;

-- =====================================================
-- Verification: Display current schema status
-- =====================================================
SELECT 'Database migration completed successfully!' AS Status;

-- Verify users table has credit_balance
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fundraising_platform' 
  AND TABLE_NAME = 'users' 
  AND COLUMN_NAME = 'credit_balance';

-- Verify payment_gateways table exists
SELECT 
    COUNT(*) AS payment_gateways_count,
    (SELECT COUNT(*) FROM payment_gateways) AS gateway_records
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'fundraising_platform' 
  AND TABLE_NAME = 'payment_gateways';

-- Verify escrow_accounts columns
SELECT 
    COLUMN_NAME, 
    DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fundraising_platform' 
  AND TABLE_NAME = 'escrow_accounts' 
  AND COLUMN_NAME IN ('total_amount', 'available_amount', 'released_amount')
ORDER BY ORDINAL_POSITION;

SELECT 'All fixes applied. You can now run your application!' AS FinalMessage;
