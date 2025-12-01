-- Fix escrow_accounts table structure
-- This script adds the missing columns to escrow_accounts table

USE fundraising_platform;

-- Add total_amount column (replaces balance functionality)
ALTER TABLE escrow_accounts 
ADD COLUMN total_amount DECIMAL(15, 2) DEFAULT 0.00 AFTER campaign_id;

-- Add available_amount column
ALTER TABLE escrow_accounts 
ADD COLUMN available_amount DECIMAL(15, 2) DEFAULT 0.00 AFTER total_amount;

-- Add released_amount column  
ALTER TABLE escrow_accounts 
ADD COLUMN released_amount DECIMAL(15, 2) DEFAULT 0.00 AFTER available_amount;

-- Add last_withdrawal_at column if missing
ALTER TABLE escrow_accounts 
ADD COLUMN last_withdrawal_at TIMESTAMP NULL AFTER released_amount;

-- Copy existing balance data to new columns
UPDATE escrow_accounts 
SET total_amount = COALESCE(balance, 0.00),
    available_amount = COALESCE(balance, 0.00),
    released_amount = 0.00
WHERE total_amount = 0.00;

SELECT 'Escrow table fixed successfully!' AS Status;

-- Verify the new structure
DESCRIBE escrow_accounts;
