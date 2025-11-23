-- CrowdAid Fundraising Platform Database Schema
-- MySQL 5.7+

CREATE DATABASE IF NOT EXISTS fundraising_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fundraising_platform;

-- Users table (base for all user types)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    role ENUM('DONOR', 'CAMPAIGNER', 'ADMIN') NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB;

-- Campaigns table
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
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaigner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_campaigner (campaigner_id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    FULLTEXT idx_search (title, description)
) ENGINE=InnoDB;

-- Milestones table
CREATE TABLE milestones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    amount DECIMAL(15, 2) NOT NULL,
    expected_date DATE,
    status ENUM('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'COMPLETED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    INDEX idx_campaign (campaign_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Evidence table
CREATE TABLE evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    milestone_id BIGINT NOT NULL,
    description TEXT,
    file_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE CASCADE,
    INDEX idx_milestone (milestone_id)
) ENGINE=InnoDB;

-- Campaign Updates table
CREATE TABLE campaign_updates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    body TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    INDEX idx_campaign (campaign_id)
) ENGINE=InnoDB;

-- Voting Periods table
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

-- Donations table
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
    INDEX idx_donor (donor_id)
) ENGINE=InnoDB;

-- Subscriptions table
CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    tier_name VARCHAR(100) NOT NULL,
    monthly_amount DECIMAL(15, 2) NOT NULL,
    status ENUM('ACTIVE', 'PAUSED', 'CANCELLED', 'EXPIRED') DEFAULT 'ACTIVE',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_campaign (campaign_id),
    INDEX idx_donor (donor_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Escrow Accounts table
CREATE TABLE escrow_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    INDEX idx_campaign (campaign_id)
) ENGINE=InnoDB;

-- Transactions table
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
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Votes table
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

-- Rewards table
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

-- Redemptions table
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

-- Credits table
CREATE TABLE credits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(10, 2) DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_donor (donor_id)
) ENGINE=InnoDB;

-- Wallets table
CREATE TABLE wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
) ENGINE=InnoDB;

-- Bank Info table
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

-- Insert default admin user (password: admin123)
INSERT INTO users (name, email, password_hash, role, verified) 
VALUES ('Admin User', 'admin@crowdaid.com', '$2a$10$YqXY.rQU0J0x8J6J7J7J6Ovl0w8j0q8qj0q8qj0q8qj0q8qj0q8qj', 'ADMIN', TRUE);

-- Insert sample rewards
INSERT INTO rewards (name, description, credit_cost, category, stock, status) VALUES
('Bronze Badge', 'Bronze supporter badge for your profile', 100.00, 'DIGITAL_BADGE', 1000, 'AVAILABLE'),
('Silver Badge', 'Silver supporter badge for your profile', 250.00, 'DIGITAL_BADGE', 1000, 'AVAILABLE'),
('Gold Badge', 'Gold supporter badge for your profile', 500.00, 'DIGITAL_BADGE', 1000, 'AVAILABLE'),
('$10 Voucher', '$10 discount voucher for partner stores', 150.00, 'VOUCHER', 100, 'AVAILABLE'),
('$25 Voucher', '$25 discount voucher for partner stores', 350.00, 'VOUCHER', 50, 'AVAILABLE'),
('Recognition Certificate', 'Official recognition certificate', 200.00, 'RECOGNITION', 500, 'AVAILABLE');
