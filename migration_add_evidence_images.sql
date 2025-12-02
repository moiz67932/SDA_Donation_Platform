-- =====================================================
-- Migration: Add Evidence Image Storage Support
-- Description: Updates the evidence table to properly
--              store milestone evidence images with 
--              metadata and timestamps
-- Date: 2025-12-01
-- Author: CrowdAid Development Team
-- =====================================================

-- =====================================================
-- IMPORTANT: Update this line with your database name
-- =====================================================
USE fundraising_platform;  -- Change 'crowdaid_db' to your actual database name

-- Check if evidence table exists, if not create it
CREATE TABLE IF NOT EXISTS evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    milestone_id BIGINT NOT NULL,
    description TEXT,
    file_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE CASCADE,
    INDEX idx_milestone (milestone_id)
) ENGINE=InnoDB;

-- Add updated_at column if it doesn't exist (for existing tables)
-- This is safe to run even if column exists
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'evidence'
  AND COLUMN_NAME = 'updated_at';

SET @alter_sql = IF(@col_exists = 0,
    'ALTER TABLE evidence ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at',
    'SELECT "Column updated_at already exists" AS message');

PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ensure file_path column has sufficient length for absolute paths
ALTER TABLE evidence MODIFY COLUMN file_path VARCHAR(1000);

-- Add comment to table
ALTER TABLE evidence COMMENT = 'Stores milestone completion evidence including image file paths';

-- Display current table structure
DESCRIBE evidence;

-- =====================================================
-- Verification Queries
-- =====================================================

-- Count existing evidence records
SELECT COUNT(*) AS total_evidence_records FROM evidence;

-- Show sample evidence records (if any exist)
SELECT 
    e.id,
    e.milestone_id,
    m.title AS milestone_title,
    e.description,
    SUBSTRING(e.file_path, 1, 50) AS file_path_preview,
    e.created_at,
    e.updated_at
FROM evidence e
LEFT JOIN milestones m ON e.milestone_id = m.id
LIMIT 5;

-- =====================================================
-- Usage Notes
-- =====================================================
-- This migration ensures the evidence table is ready to:
-- 1. Store absolute file paths for evidence images
-- 2. Track creation and update timestamps
-- 3. Link evidence to milestones with foreign key constraints
-- 4. Support multiple evidence items per milestone
--
-- After running this migration:
-- - Campaigners can upload multiple evidence images when submitting milestones
-- - Evidence images are stored with file paths in the database
-- - Donors can view evidence images when voting on milestones
-- =====================================================
