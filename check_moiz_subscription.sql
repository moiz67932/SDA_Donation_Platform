-- Targeted check for Moiz's subscription issue

USE fundraising_platform;

-- 1. Find Moiz's user ID and details
SELECT 
    'User Info:' as section,
    id as user_id,
    name,
    email,
    role
FROM users
WHERE name LIKE '%Moiz%' OR email LIKE '%moiz%';

-- 2. Show ALL subscriptions with full details
SELECT 
    'All Subscriptions:' as section,
    s.id as subscription_id,
    s.donor_id,
    u.name as donor_name,
    s.campaign_id,
    c.title as campaign_title,
    s.tier_id,
    s.tier_name,
    s.monthly_amount,
    s.status,
    s.start_date,
    s.next_billing_date
FROM subscriptions s
JOIN users u ON s.donor_id = u.id
JOIN campaigns c ON s.campaign_id = c.id
ORDER BY s.created_at DESC;

-- 3. Check if Moiz has subscriptions (by name)
SELECT 
    'Moiz Subscriptions by Name:' as section,
    s.*
FROM subscriptions s
WHERE s.donor_id IN (
    SELECT id FROM users WHERE name LIKE '%Moiz%' OR email LIKE '%moiz%'
);

-- 4. Check subscription_tiers data
SELECT 
    'Subscription Tiers:' as section,
    id,
    campaign_id,
    tier_name,
    monthly_amount,
    description
FROM subscription_tiers
ORDER BY created_at DESC;

-- 5. Test the exact query used by Java (findByDonor)
-- Replace ? with actual donor_id from query #1
-- Example: SET @donor_id = 1;
SET @donor_id = (SELECT id FROM users WHERE name LIKE '%Moiz%' LIMIT 1);

SELECT 
    'Java Query Test (findByDonor):' as section,
    s.id,
    s.campaign_id,
    s.donor_id,
    s.tier_id,
    s.tier_name,
    s.monthly_amount,
    s.status,
    s.description,
    s.start_date,
    s.next_billing_date,
    s.cancel_date,
    s.created_at,
    s.updated_at,
    st.tier_name as tier_name_from_join,
    st.description as tier_description,
    st.benefits
FROM subscriptions s
LEFT JOIN subscription_tiers st ON s.tier_id = st.id
WHERE s.donor_id = @donor_id
ORDER BY s.start_date DESC;

-- 6. Check for any data type mismatches
SELECT 
    'Data Type Check:' as section,
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'subscriptions'
AND COLUMN_NAME IN ('donor_id', 'tier_id', 'tier_name', 'status')
ORDER BY ORDINAL_POSITION;

-- 7. Check the exact status enum values
SHOW COLUMNS FROM subscriptions LIKE 'status';
