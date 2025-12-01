-- ============================================================================
-- VERIFY DATABASE TABLES
-- Run this to check if subscription tables exist
-- ============================================================================

USE fundraising_platform;

-- Check if tables exist
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME
FROM 
    INFORMATION_SCHEMA.TABLES
WHERE 
    TABLE_SCHEMA = 'fundraising_platform'
    AND TABLE_NAME IN ('subscription_tiers', 'subscriptions')
ORDER BY 
    TABLE_NAME;

-- If tables don't exist, you'll see no results above
-- In that case, the SQL didn't run properly

-- Show all tables in the database
SHOW TABLES;
