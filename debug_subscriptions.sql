-- Debug and fix subscription issues

-- 1. Check if there are any subscriptions in the database
SELECT 'Total Subscriptions:' as info, COUNT(*) as count FROM subscriptions;

-- 2. Check subscriptions by status
SELECT status, COUNT(*) as count 
FROM subscriptions 
GROUP BY status;

-- 3. Show all subscriptions with their details
SELECT 
    s.id,
    s.donor_id,
    u.name as donor_name,
    u.email as donor_email,
    s.campaign_id,
    c.title as campaign_title,
    s.tier_id,
    st.tier_name as tier_from_table,
    s.tier_name as tier_from_subscription,
    s.monthly_amount,
    s.status,
    s.start_date,
    s.next_billing_date,
    s.created_at
FROM subscriptions s
LEFT JOIN users u ON s.donor_id = u.id
LEFT JOIN campaigns c ON s.campaign_id = c.id
LEFT JOIN subscription_tiers st ON s.tier_id = st.id
ORDER BY s.created_at DESC;

-- 4. Check for any NULL tier_names (data integrity issue)
SELECT 
    s.id,
    s.donor_id,
    s.campaign_id,
    s.tier_id,
    s.tier_name,
    st.tier_name as tier_from_join
FROM subscriptions s
LEFT JOIN subscription_tiers st ON s.tier_id = st.id
WHERE s.tier_name IS NULL OR s.tier_name = '';

-- 5. Fix any subscriptions with missing tier_name
-- (Only run if query #4 found rows with NULL tier_name)
-- Uncomment and run manually if needed:
-- SET SQL_SAFE_UPDATES = 0;
-- UPDATE subscriptions s
-- LEFT JOIN subscription_tiers st ON s.tier_id = st.id
-- SET s.tier_name = st.tier_name
-- WHERE s.tier_name IS NULL OR s.tier_name = '';
-- SET SQL_SAFE_UPDATES = 1;

-- 6. Check for duplicate active subscriptions (shouldn't exist due to constraint)
SELECT 
    donor_id,
    campaign_id,
    status,
    COUNT(*) as count
FROM subscriptions
WHERE status = 'ACTIVE'
GROUP BY donor_id, campaign_id, status
HAVING count > 1;

-- 7. Verify subscription_tiers table has data
SELECT 'Total Tiers:' as info, COUNT(*) as count FROM subscription_tiers;

-- 8. Show campaigns with their tiers
SELECT 
    c.id as campaign_id,
    c.title,
    c.status as campaign_status,
    COUNT(st.id) as tier_count
FROM campaigns c
LEFT JOIN subscription_tiers st ON c.id = st.campaign_id
GROUP BY c.id, c.title, c.status
HAVING tier_count > 0
ORDER BY c.created_at DESC;

-- 9. Check if the current user (Moiz) has any subscriptions
-- Replace 'your-email@example.com' with actual email
SELECT 
    u.id as user_id,
    u.name,
    u.email,
    u.role,
    COUNT(s.id) as subscription_count
FROM users u
LEFT JOIN subscriptions s ON u.id = s.donor_id
WHERE u.name LIKE '%Moiz%' OR u.email LIKE '%moiz%'
GROUP BY u.id, u.name, u.email, u.role;

-- 10. Show detailed info for Moiz's subscriptions
SELECT 
    s.*,
    c.title as campaign_title,
    st.tier_name as tier_from_join
FROM subscriptions s
JOIN users u ON s.donor_id = u.id
LEFT JOIN campaigns c ON s.campaign_id = c.id
LEFT JOIN subscription_tiers st ON s.tier_id = st.id
WHERE u.name LIKE '%Moiz%' OR u.email LIKE '%moiz%';
