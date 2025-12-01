-- Comprehensive database fix for all missing columns and tables
USE fundraising_platform;

-- 1. Add total_withdrawn column to users table
ALTER TABLE users ADD COLUMN total_withdrawn DECIMAL(15, 2) DEFAULT 0.00 AFTER verified;

-- 2. Add credit_balance to users table
ALTER TABLE users ADD COLUMN credit_balance DECIMAL(10, 2) DEFAULT 0.00 AFTER total_withdrawn;

-- 3. Create payment_gateways table
CREATE TABLE IF NOT EXISTS payment_gateways (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. Insert default payment gateway
INSERT IGNORE INTO payment_gateways (id, name, is_active) VALUES (1, 'Simulated Payment Gateway', TRUE);

-- 5. Update NULL values
UPDATE users SET total_withdrawn = 0.00 WHERE total_withdrawn IS NULL;
UPDATE users SET credit_balance = 0.00 WHERE credit_balance IS NULL;

SELECT 'All database fixes completed successfully!' AS Status;
