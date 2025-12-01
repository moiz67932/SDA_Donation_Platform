-- ============================================================================
-- Campaign Subscription Tier Setup Script
-- This script ensures the subscription_tiers table exists and is properly configured
-- Run this script to enable tier functionality for campaigns
-- ============================================================================

USE fundraising_platform;

-- Create subscription_tiers table if it doesn't exist
CREATE TABLE IF NOT EXISTS subscription_tiers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    tier_name VARCHAR(100) NOT NULL,
    monthly_amount DECIMAL(15, 2) NOT NULL,
    description TEXT,
    benefits TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    INDEX idx_campaign (campaign_id),
    UNIQUE KEY unique_campaign_tier (campaign_id, tier_name)
) ENGINE=InnoDB;

-- Create subscriptions table if it doesn't exist
CREATE TABLE IF NOT EXISTS subscriptions (
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
    FOREIGN KEY (tier_id) REFERENCES subscription_tiers(id) ON DELETE RESTRICT,
    INDEX idx_campaign (campaign_id),
    INDEX idx_donor (donor_id),
    INDEX idx_tier (tier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Insert sample subscription tiers for existing campaigns
-- This is optional and can be commented out if not needed

-- Example: Add default tiers for campaign ID 1 (if it exists)
-- INSERT INTO subscription_tiers (campaign_id, tier_name, monthly_amount, description, benefits) 
-- VALUES 
--     (1, 'Bronze Supporter', 10.00, 'Basic support tier', 'Monthly updates\nDiscord access\nName in supporters list'),
--     (1, 'Silver Supporter', 25.00, 'Mid-tier support', 'All Bronze benefits\nExclusive Q&A sessions\nEarly access to updates'),
--     (1, 'Gold Supporter', 50.00, 'Premium support', 'All Silver benefits\nDirect communication with campaigner\nPriority support')
-- ON DUPLICATE KEY UPDATE 
--     monthly_amount = VALUES(monthly_amount),
--     description = VALUES(description),
--     benefits = VALUES(benefits);

-- Verify the tables were created
SELECT 'Subscription tables setup completed!' AS status;

SELECT 
    TABLE_NAME, 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fundraising_platform' 
  AND TABLE_NAME IN ('subscription_tiers', 'subscriptions')
ORDER BY TABLE_NAME, ORDINAL_POSITION;
