-- ============================================================================
-- QUICK FIX: Create Subscription Tables
-- Run this in MySQL Workbench or your MySQL client
-- ============================================================================

USE fundraising_platform;

-- Drop existing tables if you want a clean start (optional)
-- DROP TABLE IF EXISTS subscriptions;
-- DROP TABLE IF EXISTS subscription_tiers;

-- Create subscription_tiers table
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

-- Create subscriptions table
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

-- Verify tables were created
SHOW TABLES LIKE 'subscription%';

SELECT 'SUCCESS! Tables created. You can now add tiers to your campaigns!' AS message;
