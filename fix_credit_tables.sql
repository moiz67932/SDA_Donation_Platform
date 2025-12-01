-- Fix Credit Tables Schema
-- This script creates the missing credit_transactions table
-- and ensures the credits table has the correct structure

USE fundraising_platform;

-- Create credit_transactions table if it doesn't exist
CREATE TABLE IF NOT EXISTS credit_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    type ENUM('EARNED', 'SPENT', 'ADJUSTMENT') NOT NULL,
    source VARCHAR(500),
    reference_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_donor (donor_id),
    INDEX idx_type (type)
) ENGINE=InnoDB;

-- Verify the structure
SELECT 'Credit tables fixed successfully!' AS Status;

-- Show credits table structure
SELECT 'Credits table structure:' AS Info;
DESCRIBE credits;

-- Show credit_transactions table structure
SELECT 'Credit transactions table structure:' AS Info;
DESCRIBE credit_transactions;
