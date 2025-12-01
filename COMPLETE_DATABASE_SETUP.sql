-- ============================================================================
-- CrowdAid Fundraising Platform - Complete Database Setup Script
-- ============================================================================
-- This is a comprehensive script that combines all database requirements
-- Run this script to create a fresh database with all tables and initial data
-- MySQL 5.7+ Required
-- ============================================================================
-- Version: 1.0
-- Date: November 30, 2025
-- Description: Complete database recreation script for CrowdAid Platform
-- ============================================================================

-- ============================================================================
-- SECTION 1: DATABASE INITIALIZATION
-- ============================================================================

-- Drop existing database if needed (WARNING: This will delete all existing data)
-- Uncomment the line below if you want to start fresh
-- DROP DATABASE IF EXISTS fundraising_platform;

-- Create database with UTF-8 support
CREATE DATABASE IF NOT EXISTS fundraising_platform 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE fundraising_platform;

-- Disable foreign key checks temporarily for table creation
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- SECTION 2: CORE USER TABLES
-- ============================================================================

-- Users table (base for all user types: Donors, Campaigners, Admins)
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    role ENUM('DONOR', 'CAMPAIGNER', 'ADMIN') NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    total_withdrawn DECIMAL(15, 2) DEFAULT 0.00,
    credit_balance DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB;

-- Wallets table (user balance management)
DROP TABLE IF EXISTS wallets;
CREATE TABLE wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
) ENGINE=InnoDB;

-- Bank Info table (for withdrawals)
DROP TABLE IF EXISTS bank_info;
CREATE TABLE bank_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    account_holder_name VARCHAR(255) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    routing_number VARCHAR(50),
    swift_code VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
) ENGINE=InnoDB;

-- ============================================================================
-- SECTION 3: CAMPAIGN TABLES
-- ============================================================================

-- Campaigns table
DROP TABLE IF EXISTS campaigns;
CREATE TABLE campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaigner_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    goal_amount DECIMAL(15, 2) NOT NULL,
    collected_amount DECIMAL(15, 2) DEFAULT 0.00,
    category ENUM('MEDICAL', 'EDUCATION', 'EMERGENCY', 'COMMUNITY', 'CREATIVE', 'BUSINESS', 'NONPROFIT', 'ENVIRONMENTAL', 'CIVIC', 'OTHER') NOT NULL,
    status ENUM('PENDING_REVIEW', 'ACTIVE', 'REJECTED', 'SUSPENDED', 'COMPLETED', 'ENDED', 'CANCELLED') DEFAULT 'PENDING_REVIEW',
    start_date DATE,
    end_date DATE,
    is_philanthropic BOOLEAN DEFAULT FALSE,
    is_civic BOOLEAN DEFAULT FALSE,
    is_escrow_enabled BOOLEAN DEFAULT FALSE,
    is_reward_eligible BOOLEAN DEFAULT FALSE,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaigner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_campaigner (campaigner_id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_escrow_enabled (is_escrow_enabled),
    FULLTEXT idx_search (title, description)
) ENGINE=InnoDB;

-- Campaign Updates table (for posting updates to campaigns)
DROP TABLE IF EXISTS campaign_updates;
CREATE TABLE campaign_updates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    body TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    INDEX idx_campaign (campaign_id)
) ENGINE=InnoDB;

-- ============================================================================
-- SECTION 4: MILESTONE & VOTING TABLES
-- ============================================================================

-- Milestones table
DROP TABLE IF EXISTS milestones;
CREATE TABLE milestones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    amount DECIMAL(15, 2) NOT NULL,
    expected_date DATE,
    status ENUM('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'COMPLETED', 'RELEASED') DEFAULT 'PENDING',
    released_amount DECIMAL(15, 2) DEFAULT 0.00,
    released_at TIMESTAMP NULL,
    is_withdrawn BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    INDEX idx_campaign (campaign_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Evidence table (for milestone completion evidence)
DROP TABLE IF EXISTS evidence;
CREATE TABLE evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    milestone_id BIGINT NOT NULL,
    description TEXT,
    file_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE CASCADE,
    INDEX idx_milestone (milestone_id)
) ENGINE=InnoDB;

-- Voting Periods table
DROP TABLE IF EXISTS voting_periods;
CREATE TABLE voting_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    milestone_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE CASCADE,
    INDEX idx_milestone (milestone_id),
    INDEX idx_active (active)
) ENGINE=InnoDB;

-- Votes table
DROP TABLE IF EXISTS votes;
CREATE TABLE votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    milestone_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    vote_type ENUM('APPROVE', 'REJECT') NOT NULL,
    weight DECIMAL(15, 2) DEFAULT 1.00,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_vote (milestone_id, donor_id),
    INDEX idx_milestone (milestone_id),
    INDEX idx_donor (donor_id)
) ENGINE=InnoDB;

-- ============================================================================
-- SECTION 5: DONATION & PAYMENT TABLES
-- ============================================================================

-- Donations table
DROP TABLE IF EXISTS donations;
CREATE TABLE donations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    is_anonymous BOOLEAN DEFAULT FALSE,
    message TEXT,
    transaction_reference VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_campaign (campaign_id),
    INDEX idx_donor (donor_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- Subscription Tiers table
DROP TABLE IF EXISTS subscription_tiers;
CREATE TABLE subscription_tiers (
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

-- Subscriptions table (for recurring donations)
DROP TABLE IF EXISTS subscriptions;
CREATE TABLE subscriptions (
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
    INDEX idx_status (status),
    INDEX idx_next_billing (next_billing_date)
) ENGINE=InnoDB;

-- Escrow Accounts table (holds funds until milestone approval)
DROP TABLE IF EXISTS escrow_accounts;
CREATE TABLE escrow_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    total_amount DECIMAL(15, 2) DEFAULT 0.00,
    available_amount DECIMAL(15, 2) DEFAULT 0.00,
    released_amount DECIMAL(15, 2) DEFAULT 0.00,
    last_withdrawal_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    INDEX idx_campaign (campaign_id)
) ENGINE=InnoDB;

-- Payment Gateways table
DROP TABLE IF EXISTS payment_gateways;
CREATE TABLE payment_gateways (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Transactions table (tracks all financial movements)
DROP TABLE IF EXISTS transactions;
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    escrow_id BIGINT,
    campaign_id BIGINT NOT NULL,
    donor_id BIGINT,
    amount DECIMAL(15, 2) NOT NULL,
    type ENUM('DONATION_IN', 'SUBSCRIPTION_IN', 'ESCROW_RELEASE', 'REFUND', 'PLATFORM_FEE', 'CREDIT_EARNED', 'CREDIT_SPENT') NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'PROCESSING', 'REFUNDED') DEFAULT 'PENDING',
    reference VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (escrow_id) REFERENCES escrow_accounts(id) ON DELETE SET NULL,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_campaign (campaign_id),
    INDEX idx_donor (donor_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- ============================================================================
-- SECTION 6: REWARD SYSTEM TABLES
-- ============================================================================

-- Credits table (donor reward points)
DROP TABLE IF EXISTS credits;
CREATE TABLE credits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(10, 2) DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_donor (donor_id)
) ENGINE=InnoDB;

-- Credit Transactions table (tracks credit earning and spending)
DROP TABLE IF EXISTS credit_transactions;
CREATE TABLE credit_transactions (
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

-- Rewards table (items available in reward shop)
DROP TABLE IF EXISTS rewards;
CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    credit_cost DECIMAL(10, 2) NOT NULL,
    category ENUM('DIGITAL_BADGE', 'VOUCHER', 'MERCHANDISE', 'RECOGNITION', 'EXCLUSIVE_CONTENT') NOT NULL,
    stock INT DEFAULT 0,
    status ENUM('AVAILABLE', 'OUT_OF_STOCK', 'DISABLED') DEFAULT 'AVAILABLE',
    image_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB;

-- Redemptions table (tracks reward purchases)
DROP TABLE IF EXISTS redemptions;
CREATE TABLE redemptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reward_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    credits_spent DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'CANCELLED', 'FAILED') DEFAULT 'PENDING',
    delivery_info TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reward_id) REFERENCES rewards(id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reward (reward_id),
    INDEX idx_donor (donor_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- SECTION 7: INITIAL DATA INSERTION
-- ============================================================================

-- Insert default admin user
-- Email: admin@crowdaid.com
-- Password: admin123 (hashed with BCrypt)
INSERT INTO users (name, email, password_hash, role, verified) 
VALUES ('Admin User', 'admin@crowdaid.com', '$2a$10$/20Q1uqfRzJm1.YIk.y0cu.quX6z9xXXCZHK2f4mTyXdvk2a0ZDcC', 'ADMIN', TRUE);

-- Insert default payment gateway
INSERT INTO payment_gateways (name, is_active) 
VALUES ('Simulated Payment Gateway', TRUE);

-- Insert sample rewards for the reward shop
INSERT INTO rewards (name, description, credit_cost, category, stock, status) VALUES
('Bronze Badge', 'Bronze supporter badge for your profile', 100.00, 'DIGITAL_BADGE', 1000, 'AVAILABLE'),
('Silver Badge', 'Silver supporter badge for your profile', 250.00, 'DIGITAL_BADGE', 1000, 'AVAILABLE'),
('Gold Badge', 'Gold supporter badge for your profile', 500.00, 'DIGITAL_BADGE', 1000, 'AVAILABLE'),
('Platinum Badge', 'Platinum supporter badge for your profile', 1000.00, 'DIGITAL_BADGE', 500, 'AVAILABLE'),
('Diamond Badge', 'Diamond supporter badge - ultimate recognition', 2000.00, 'DIGITAL_BADGE', 250, 'AVAILABLE'),
('$10 Voucher', '$10 discount voucher for partner stores', 150.00, 'VOUCHER', 100, 'AVAILABLE'),
('$25 Voucher', '$25 discount voucher for partner stores', 350.00, 'VOUCHER', 50, 'AVAILABLE'),
('$50 Voucher', '$50 discount voucher for partner stores', 600.00, 'VOUCHER', 25, 'AVAILABLE'),
('$100 Voucher', '$100 discount voucher for partner stores', 1100.00, 'VOUCHER', 10, 'AVAILABLE'),
('Recognition Certificate', 'Official recognition certificate', 200.00, 'RECOGNITION', 500, 'AVAILABLE'),
('Thank You Letter', 'Personalized thank you letter', 50.00, 'RECOGNITION', 1000, 'AVAILABLE'),
('Commemorative Plaque', 'Physical commemorative plaque', 800.00, 'MERCHANDISE', 50, 'AVAILABLE'),
('Exclusive Newsletter', 'Access to monthly exclusive newsletter', 300.00, 'EXCLUSIVE_CONTENT', 200, 'AVAILABLE'),
('VIP Content Access', 'Access to VIP exclusive content for 6 months', 500.00, 'EXCLUSIVE_CONTENT', 100, 'AVAILABLE'),
('Behind The Scenes', 'Access to behind-the-scenes campaign content', 400.00, 'EXCLUSIVE_CONTENT', 150, 'AVAILABLE');

-- ============================================================================
-- SECTION 8: DATABASE VERIFICATION
-- ============================================================================

-- Display created tables
SELECT 'Database setup completed successfully!' AS Status;

SELECT '========================================' AS Separator;
SELECT 'VERIFICATION: List of all tables created' AS Info;
SELECT '========================================' AS Separator;

SHOW TABLES;

SELECT '========================================' AS Separator;
SELECT 'VERIFICATION: Admin user count' AS Info;
SELECT '========================================' AS Separator;

SELECT COUNT(*) AS admin_count FROM users WHERE role='ADMIN';

SELECT '========================================' AS Separator;
SELECT 'VERIFICATION: Total rewards available' AS Info;
SELECT '========================================' AS Separator;

SELECT COUNT(*) AS total_rewards FROM rewards;

SELECT '========================================' AS Separator;
SELECT 'VERIFICATION: Payment gateways configured' AS Info;
SELECT '========================================' AS Separator;

SELECT * FROM payment_gateways;

-- ============================================================================
-- SECTION 9: USEFUL VERIFICATION QUERIES
-- ============================================================================

-- Uncomment these queries to verify specific aspects of your database:

-- Check all users
-- SELECT * FROM users;

-- Check all campaigns
-- SELECT * FROM campaigns;

-- Check all rewards
-- SELECT * FROM rewards;

-- Check escrow accounts
-- SELECT * FROM escrow_accounts;

-- Check subscription tiers
-- SELECT * FROM subscription_tiers;

-- Check database size
-- SELECT 
--     table_name AS 'Table',
--     ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
-- FROM information_schema.TABLES 
-- WHERE table_schema = 'fundraising_platform'
-- ORDER BY (data_length + index_length) DESC;

-- ============================================================================
-- END OF SCRIPT
-- ============================================================================
-- Next Steps:
-- 1. Verify all tables are created: SHOW TABLES;
-- 2. Check admin user: SELECT * FROM users WHERE role='ADMIN';
-- 3. Test the application with this database
-- 4. Optionally run sample_data.sql for test data
-- ============================================================================
