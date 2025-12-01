-- ============================================================================
-- Campaign Tier System - Complete Test Script
-- This script sets up sample data for testing the tier functionality
-- ============================================================================

USE fundraising_platform;

-- ============================================================================
-- STEP 1: Verify Tables Exist
-- ============================================================================

SELECT 'Checking subscription_tiers table...' AS status;
SHOW CREATE TABLE subscription_tiers;

SELECT 'Checking subscriptions table...' AS status;
SHOW CREATE TABLE subscriptions;

-- ============================================================================
-- STEP 2: Clean Up Existing Test Data (Optional)
-- ============================================================================

-- Uncomment to remove existing test tiers
-- DELETE FROM subscription_tiers WHERE campaign_id IN (SELECT id FROM campaigns WHERE title LIKE '%Test%');

-- ============================================================================
-- STEP 3: Insert Sample Subscription Tiers for Testing
-- ============================================================================

-- Find an active campaign to add tiers to
SET @test_campaign_id = (SELECT id FROM campaigns WHERE status = 'ACTIVE' LIMIT 1);

-- If no active campaign exists, create one for testing
INSERT INTO campaigns (campaigner_id, title, description, goal_amount, collected_amount, category, status, start_date)
SELECT 
    id,
    'Test Campaign for Subscription Tiers',
    'This is a test campaign to demonstrate the subscription tier functionality. Donors can subscribe with recurring monthly payments.',
    10000.00,
    0.00,
    'COMMUNITY',
    'ACTIVE',
    CURDATE()
FROM users 
WHERE role = 'CAMPAIGNER' 
LIMIT 1
ON DUPLICATE KEY UPDATE id=id;

-- Get the campaign ID
SET @test_campaign_id = LAST_INSERT_ID();
IF @test_campaign_id = 0 THEN
    SET @test_campaign_id = (SELECT id FROM campaigns WHERE title = 'Test Campaign for Subscription Tiers' LIMIT 1);
END IF;

-- Add subscription tiers for the test campaign
INSERT INTO subscription_tiers (campaign_id, tier_name, monthly_amount, description, benefits) 
VALUES 
    (@test_campaign_id, 'Bronze Supporter', 10.00, 
     'Basic support tier for those who want to contribute regularly', 
     '- Monthly campaign updates via email\n- Name listed in supporters section\n- Access to supporter-only Discord channel'),
    
    (@test_campaign_id, 'Silver Supporter', 25.00, 
     'Mid-tier support with enhanced benefits', 
     '- All Bronze benefits\n- Quarterly Q&A sessions with campaigner\n- Early access to campaign updates\n- Special recognition badge'),
    
    (@test_campaign_id, 'Gold Supporter', 50.00, 
     'Premium support tier with exclusive access', 
     '- All Silver benefits\n- Direct monthly video calls with campaigner\n- Priority support and responses\n- Exclusive behind-the-scenes content\n- Input on campaign decisions'),
    
    (@test_campaign_id, 'Platinum Supporter', 100.00, 
     'Ultimate support tier for top backers', 
     '- All Gold benefits\n- Personal shoutout on social media\n- VIP event access (if applicable)\n- Co-creation opportunities\n- Special commemorative item')
ON DUPLICATE KEY UPDATE 
    monthly_amount = VALUES(monthly_amount),
    description = VALUES(description),
    benefits = VALUES(benefits);

-- ============================================================================
-- STEP 4: Verify Tier Creation
-- ============================================================================

SELECT 'Sample subscription tiers created!' AS status;

SELECT 
    st.id,
    c.title AS campaign_title,
    st.tier_name,
    st.monthly_amount,
    st.description,
    st.benefits,
    st.created_at
FROM subscription_tiers st
JOIN campaigns c ON st.campaign_id = c.id
WHERE st.campaign_id = @test_campaign_id
ORDER BY st.monthly_amount ASC;

-- ============================================================================
-- STEP 5: Check for Existing Subscriptions
-- ============================================================================

SELECT 'Checking existing subscriptions...' AS status;

SELECT 
    s.id,
    c.title AS campaign_title,
    u.name AS donor_name,
    u.email AS donor_email,
    st.tier_name,
    s.monthly_amount,
    s.status,
    s.start_date,
    s.next_billing_date
FROM subscriptions s
JOIN campaigns c ON s.campaign_id = c.id
JOIN users u ON s.donor_id = u.id
JOIN subscription_tiers st ON s.tier_id = st.id
WHERE s.campaign_id = @test_campaign_id
ORDER BY s.created_at DESC;

-- ============================================================================
-- STEP 6: Display Summary
-- ============================================================================

SELECT 
    '=== SUBSCRIPTION TIER SYSTEM TEST SUMMARY ===' AS summary
UNION ALL
SELECT CONCAT('Test Campaign ID: ', @test_campaign_id)
UNION ALL
SELECT CONCAT('Total Tiers Created: ', COUNT(*)) 
FROM subscription_tiers 
WHERE campaign_id = @test_campaign_id
UNION ALL
SELECT CONCAT('Total Active Subscriptions: ', COUNT(*)) 
FROM subscriptions 
WHERE campaign_id = @test_campaign_id AND status = 'ACTIVE';

-- ============================================================================
-- STEP 7: Useful Queries for Testing
-- ============================================================================

-- Query to find all campaigns with subscription tiers
SELECT 
    c.id,
    c.title,
    c.status,
    COUNT(st.id) AS tier_count,
    MIN(st.monthly_amount) AS min_tier_amount,
    MAX(st.monthly_amount) AS max_tier_amount
FROM campaigns c
LEFT JOIN subscription_tiers st ON c.id = st.campaign_id
WHERE c.status IN ('ACTIVE', 'PENDING_REVIEW')
GROUP BY c.id, c.title, c.status
HAVING tier_count > 0
ORDER BY tier_count DESC;

-- Query to check subscription distribution across tiers
SELECT 
    st.tier_name,
    st.monthly_amount,
    COUNT(s.id) AS subscription_count,
    SUM(CASE WHEN s.status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_subscriptions,
    SUM(CASE WHEN s.status = 'ACTIVE' THEN s.monthly_amount ELSE 0 END) AS monthly_recurring_revenue
FROM subscription_tiers st
LEFT JOIN subscriptions s ON st.id = s.tier_id
GROUP BY st.id, st.tier_name, st.monthly_amount
ORDER BY st.monthly_amount ASC;

SELECT 'Test script completed successfully!' AS status;
SELECT 'You can now test the tier functionality in the application.' AS next_step;
