-- Sample Data for Testing CrowdAid Platform
-- Run this after creating the database schema

USE fundraising_platform;

-- Insert sample campaigns (campaigner_id=2 is the registered user)
INSERT INTO campaigns (campaigner_id, title, description, goal_amount, collected_amount, category, status, start_date, end_date, is_philanthropic, is_civic) VALUES
(2, 'Medical Treatment for Cancer Patient', 'Help fund life-saving cancer treatment for a young mother of two.', 50000.00, 12500.00, 'MEDICAL', 'ACTIVE', '2025-11-01', '2025-12-31', TRUE, FALSE),
(2, 'Build Community Center', 'Construction of a community center for underprivileged children.', 100000.00, 45000.00, 'COMMUNITY', 'ACTIVE', '2025-11-10', '2026-02-28', FALSE, TRUE),
(2, 'Emergency Flood Relief', 'Provide immediate relief to families affected by recent floods.', 25000.00, 18000.00, 'EMERGENCY', 'ACTIVE', '2025-11-15', '2025-12-15', TRUE, FALSE);

-- Insert milestones for campaigns
INSERT INTO milestones (campaign_id, title, description, amount, expected_date, status) VALUES
(1, 'Initial Consultation', 'Complete initial medical consultation and tests', 5000.00, '2025-11-20', 'COMPLETED'),
(1, 'Surgery Phase', 'Surgical procedure and hospital stay', 30000.00, '2025-12-10', 'PENDING'),
(1, 'Post-Treatment Care', 'Follow-up treatments and medications', 15000.00, '2026-01-15', 'PENDING'),
(2, 'Land Acquisition', 'Purchase land for community center', 30000.00, '2025-12-01', 'APPROVED'),
(2, 'Foundation Work', 'Complete foundation and basic structure', 40000.00, '2026-01-15', 'PENDING'),
(2, 'Interior Setup', 'Furnish and equip the center', 30000.00, '2026-02-20', 'PENDING');

-- Insert sample donations (donor_id=2 from registered user)
INSERT INTO donations (campaign_id, donor_id, amount, is_anonymous, message, transaction_reference, created_at) VALUES
(1, 2, 100.00, FALSE, 'Wishing you a speedy recovery!', 'TXN001', '2025-11-16 10:30:00'),
(2, 2, 250.00, FALSE, 'Great initiative for the community', 'TXN002', '2025-11-17 14:15:00'),
(3, 2, 50.00, TRUE, '', 'TXN003', '2025-11-18 09:45:00');

-- Update campaign collected amounts (with WHERE clause for safe update mode)
UPDATE campaigns SET collected_amount = (
    SELECT COALESCE(SUM(amount), 0) FROM donations WHERE campaign_id = campaigns.id
) WHERE id IN (1, 2, 3);

-- Insert sample escrow accounts
INSERT INTO escrow_accounts (campaign_id, balance) VALUES
(1, 12500.00),
(2, 45000.00),
(3, 18000.00);

-- Insert sample transactions
INSERT INTO transactions (escrow_id, campaign_id, donor_id, amount, type, status, reference, description, created_at) VALUES
(1, 1, 2, 100.00, 'DONATION_IN', 'SUCCESS', 'TXN001', 'Donation received', '2025-11-16 10:30:00'),
(2, 2, 2, 250.00, 'DONATION_IN', 'SUCCESS', 'TXN002', 'Donation received', '2025-11-17 14:15:00'),
(3, 3, 2, 50.00, 'DONATION_IN', 'SUCCESS', 'TXN003', 'Donation received', '2025-11-18 09:45:00');

-- Insert credits for donor (10% of donation as credits)
INSERT INTO credits (donor_id, balance) VALUES
(2, 40.00)
ON DUPLICATE KEY UPDATE balance = balance + 40.00;

-- Update rewards stock (already inserted in schema)
UPDATE rewards SET stock = 1000 WHERE category = 'DIGITAL_BADGE';
UPDATE rewards SET stock = 100 WHERE category = 'VOUCHER';
UPDATE rewards SET stock = 500 WHERE category = 'RECOGNITION';

-- Insert campaign updates
INSERT INTO campaign_updates (campaign_id, title, body, created_at) VALUES
(1, 'Thank You for Your Support!', 'We have reached 25% of our goal! The patient has completed initial consultations.', '2025-11-18 16:00:00'),
(2, 'Land Purchase Complete', 'Great news! We have successfully purchased the land for the community center.', '2025-11-19 11:30:00');

-- Insert pending campaigns for admin approval
INSERT INTO campaigns (campaigner_id, title, description, goal_amount, collected_amount, category, status, start_date, end_date) VALUES
(2, 'Education Scholarship Fund', 'Provide scholarships for underprivileged students', 75000.00, 0.00, 'EDUCATION', 'PENDING_REVIEW', '2025-12-01', '2026-05-31');

COMMIT;
