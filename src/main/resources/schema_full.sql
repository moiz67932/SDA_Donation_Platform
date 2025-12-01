-- schema_full.sql
-- CrowdAid / SDA Donation Platform
-- Database: fundraiser_db

DROP DATABASE IF EXISTS fundraiser_db;
CREATE DATABASE fundraiser_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fundraiser_db;

-- =========================
-- Users & Roles
-- =========================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(30),
    role ENUM('DONOR','CAMPAIGNER','ADMIN') NOT NULL,
    is_verified TINYINT(1) NOT NULL DEFAULT 0,
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE donors (
    user_id BIGINT PRIMARY KEY,
    credit_balance INT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE campaigners (
    user_id BIGINT PRIMARY KEY,
    total_funds_raised DECIMAL(12,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE admins (
    user_id BIGINT PRIMARY KEY,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE bank_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaigner_id BIGINT NOT NULL,
    account_title VARCHAR(150) NOT NULL,
    iban VARCHAR(50) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (campaigner_id) REFERENCES campaigners(user_id)
);

-- =========================
-- Campaigns & Media
-- =========================

CREATE TABLE campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaigner_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    funding_goal DECIMAL(12,2) NOT NULL,
    current_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    category VARCHAR(100) NOT NULL,
    status ENUM('PENDING','ACTIVE','COMPLETED','REJECTED','CLOSED') NOT NULL DEFAULT 'PENDING',
    start_date DATE NOT NULL,
    end_date DATE,
    view_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaigner_id) REFERENCES campaigners(user_id)
);

CREATE TABLE campaign_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    file_url VARCHAR(255) NOT NULL,
    media_type ENUM('IMAGE','VIDEO') NOT NULL DEFAULT 'IMAGE',
    is_primary TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id)
);

CREATE TABLE campaign_updates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    title VARCHAR(150),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id)
);

-- =========================
-- Escrow & Milestones
-- =========================

CREATE TABLE escrow_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL UNIQUE,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    available_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    released_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id)
);

CREATE TABLE milestones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    fund_amount DECIMAL(12,2) NOT NULL,
    expected_date DATE,
    status ENUM('PLANNED','UNDER_REVIEW','APPROVED','REJECTED','RELEASED') NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id)
);

CREATE TABLE evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    milestone_id BIGINT NOT NULL,
    file_uri VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    description TEXT,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (milestone_id) REFERENCES milestones(id)
);

CREATE TABLE voting_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    milestone_id BIGINT NOT NULL UNIQUE,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    approval_count INT NOT NULL DEFAULT 0,
    rejection_count INT NOT NULL DEFAULT 0,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    FOREIGN KEY (milestone_id) REFERENCES milestones(id)
);

-- =========================
-- Donations, Subscriptions, Transactions
-- =========================

CREATE TABLE subscription_tiers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    benefits TEXT,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id)
);

CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL,
    campaign_id BIGINT NOT NULL,
    tier_id BIGINT NOT NULL,
    monthly_amount DECIMAL(12,2) NOT NULL,
    start_date DATE NOT NULL,
    next_payment_date DATE,
    status ENUM('ACTIVE','PAUSED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (donor_id) REFERENCES donors(user_id),
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id),
    FOREIGN KEY (tier_id) REFERENCES subscription_tiers(id)
);

CREATE TABLE donations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL,
    campaign_id BIGINT NOT NULL,
    subscription_id BIGINT NULL,
    amount DECIMAL(12,2) NOT NULL,
    donation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_anonymous TINYINT(1) NOT NULL DEFAULT 0,
    message TEXT,
    donation_type ENUM('ONE_TIME','SUBSCRIPTION') NOT NULL DEFAULT 'ONE_TIME',
    transaction_id BIGINT NULL,
    FOREIGN KEY (donor_id) REFERENCES donors(user_id),
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id),
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id)
);

CREATE TABLE payment_gateways (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NULL,
    gateway_id BIGINT NULL,
    amount DECIMAL(12,2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    type ENUM('DONATION','SUBSCRIPTION_CHARGE','REFUND','REWARD_REDEMPTION','ESCROW_RELEASE') NOT NULL,
    status ENUM('PENDING','SUCCESS','FAILED') NOT NULL,
    payment_method VARCHAR(50),
    reference TEXT,
    FOREIGN KEY (donor_id) REFERENCES donors(user_id),
    FOREIGN KEY (gateway_id) REFERENCES payment_gateways(id)
);

-- =========================
-- Voting
-- =========================

CREATE TABLE votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL,
    milestone_id BIGINT NOT NULL,
    vote_type ENUM('APPROVE','REJECT') NOT NULL,
    weight DECIMAL(6,2) NOT NULL DEFAULT 1,
    vote_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comment TEXT,
    UNIQUE KEY uq_vote_donor_milestone (donor_id, milestone_id),
    FOREIGN KEY (donor_id) REFERENCES donors(user_id),
    FOREIGN KEY (milestone_id) REFERENCES milestones(id)
);

-- =========================
-- Credits & Rewards
-- =========================

CREATE TABLE credits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL,
    amount INT NOT NULL,
    earned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source VARCHAR(255),
    FOREIGN KEY (donor_id) REFERENCES donors(user_id)
);

CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    credit_cost INT NOT NULL,
    category ENUM('DIGITAL','PHYSICAL','OTHER') NOT NULL DEFAULT 'OTHER',
    stock_quantity INT NOT NULL DEFAULT 0,
    is_available TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE redemptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    credits_used INT NOT NULL,
    status ENUM('PENDING','FULFILLED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    redemption_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES donors(user_id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
);

-- =========================
-- Notifications
-- =========================

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(150),
    message TEXT NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================
-- Indexes & Seed Data
-- =========================

CREATE INDEX idx_campaign_status ON campaigns(status);
CREATE INDEX idx_donations_campaign ON donations(campaign_id);
CREATE INDEX idx_donations_donor ON donations(donor_id);
CREATE INDEX idx_subscriptions_donor ON subscriptions(donor_id);
CREATE INDEX idx_votes_milestone ON votes(milestone_id);

-- Optional seed admin (password can be plain; the app can hash it later if needed)
INSERT INTO users (name, email, password_hash, role, is_verified)
VALUES ('Admin', 'admin@crowdaid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 1);
INSERT INTO admins (user_id)
SELECT id FROM users WHERE email = 'admin@crowdaid.com';

-- Insert simulated payment gateway
INSERT INTO payment_gateways (name) VALUES ('Simulated Payment Gateway');
