# Evidence Image Upload Feature - Implementation Guide

## Overview
This implementation adds the ability for campaigners to upload evidence images when submitting payment requests for milestones in escrow campaigns. Donors can then view these images when voting on milestone completion.

## Changes Made

### 1. Frontend UI Updates

#### Submit Milestone Screen (`submit_milestone.fxml`)
- **Changed**: Replaced text field inputs with FileChooser-based image selection
- **Added**: Three file chooser buttons for selecting evidence images
- **Added**: Clear buttons for each image slot
- **Added**: ListView to display selected file names
- **Features**: 
  - First image is required, additional two are optional
  - Read-only text fields show selected file names
  - Better user experience with browse dialogs

#### Voting Requests Screen (`voting_requests.fxml`)
- **Added**: Evidence display section showing:
  - ListView of evidence file names
  - TextArea with detailed evidence information (file path, description, upload time)
- **Layout**: Side-by-side display of evidence files and details
- **Auto-load**: Evidence loads automatically when donor selects a milestone

### 2. Controller Updates

#### SubmitMilestoneController.java
**New Imports**:
- `Evidence` - Model for evidence objects
- `FileChooser` - For file selection dialog
- `File` - For file handling

**New Fields**:
- `List<File> evidenceFiles` - Stores selected image files
- `ALLOWED_IMAGE_EXTENSIONS` - Validates image file types (.jpg, .jpeg, .png, .gif, .bmp)

**New Methods**:
- `handleChooseImage1/2/3()` - Opens file chooser for image selection
- `handleClearImage1/2/3()` - Clears selected images
- `chooseImageFile()` - Handles file selection with validation
- `clearImageFile()` - Removes selected file
- `isValidImageFile()` - Validates file is an image
- `updateEvidenceListView()` - Updates UI with selected files

**Updated Methods**:
- `handleSubmit()` - Creates Evidence objects from selected files and submits to service

#### VotingRequestsController.java
**New Imports**:
- `Evidence` - Model for evidence
- `File` - For file path handling
- `List` - For evidence collections

**New Fields**:
- `@FXML ListView<String> evidenceListView` - Displays evidence file names
- `@FXML TextArea evidenceDetailsArea` - Shows detailed evidence info

**New Methods**:
- `loadMilestoneEvidence()` - Fetches and displays evidence for selected milestone
- `clearEvidenceDisplay()` - Clears evidence when no milestone selected

**Updated Methods**:
- `initialize()` - Added listener to load evidence on milestone selection

### 3. Service Layer Updates

#### MilestoneService.java
**New Dependencies**:
- `EvidenceRepository` - For evidence database operations
- `Evidence` - Model class

**Updated Constructor**:
- Initializes `MySQLEvidenceRepository`

**Replaced Method**:
```java
// OLD
submitMilestoneCompletion(Long milestoneId, String evidenceDescription, String evidenceUrl)

// NEW
submitMilestoneCompletion(Long milestoneId, List<Evidence> evidenceList, String completionDescription)
```

**New Method**:
- `getMilestoneEvidence(Long milestoneId)` - Retrieves evidence for a milestone

**Behavior Changes**:
- Now saves each evidence item to database using `evidenceRepository.save()`
- Validates at least one evidence item is provided
- Updates milestone status to UNDER_REVIEW after evidence submission

### 4. Repository Layer (NEW)

#### EvidenceRepository.java (Interface)
**Methods**:
- `save(Evidence)` - Saves evidence to database
- `findById(Long)` - Retrieves evidence by ID
- `findByMilestone(Long)` - Gets all evidence for a milestone
- `delete(Long)` - Deletes evidence by ID
- `deleteByMilestone(Long)` - Deletes all evidence for a milestone

#### MySQLEvidenceRepository.java (Implementation)
**Key Features**:
- Full CRUD operations for evidence table
- Proper SQL prepared statements to prevent injection
- Automatic ID generation on insert
- ResultSet mapping to Evidence objects
- Logging for all operations

### 5. Database Migration

#### migration_add_evidence_images.sql
**Creates/Updates**:
- `evidence` table with proper schema
- Adds `updated_at` column if missing
- Increases `file_path` column size to 1000 chars for long paths
- Foreign key constraint to milestones table
- Index on milestone_id for performance

**Schema**:
```sql
CREATE TABLE evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    milestone_id BIGINT NOT NULL,
    description TEXT,
    file_path VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE CASCADE,
    INDEX idx_milestone (milestone_id)
)
```

## How It Works

### Campaigner Workflow
1. **Navigate** to "Submit Milestone" from campaigner dashboard
2. **Select** a pending milestone from dropdown
3. **Write** completion description
4. **Click** "Choose Image" buttons to select evidence images
5. **Browse** for image files (jpg, jpeg, png, gif, bmp)
6. **Review** selected files in the list view
7. **Submit** - Evidence images are saved to database with file paths
8. **Status** changes to UNDER_REVIEW, triggering donor voting

### Donor Workflow
1. **Navigate** to "Voting Requests" from donor dashboard
2. **View** table of milestones awaiting votes
3. **Select** a milestone - evidence loads automatically
4. **Review** evidence files and details:
   - File names in left panel
   - File paths, descriptions, upload times in right panel
5. **Add** optional comment
6. **Vote** Approve or Reject based on evidence

## Technical Details

### File Storage
- **Current**: Files remain in their original location
- **Database**: Only file paths (absolute paths) are stored
- **Validation**: Only image files with allowed extensions accepted
- **Size**: No file size limit enforced (add if needed)

### Error Handling
- Validates at least one evidence image required
- File type validation prevents non-image uploads
- Database transaction handling in repositories
- User-friendly error messages via AlertUtil
- Comprehensive logging for debugging

### Security Considerations
- File paths stored as-is (consider sanitization for production)
- No file upload to server (files stay on client machine)
- SQL injection prevented with PreparedStatements
- Foreign key constraints ensure data integrity

## Running the Migration

### Steps:
1. **Backup** your database first!
2. **Open** MySQL client or MySQL Workbench
3. **Connect** to your CrowdAid database
4. **Run** the migration file:
   ```bash
   mysql -u your_username -p crowdaid_db < migration_add_evidence_images.sql
   ```
   Or in MySQL Workbench: Open and execute the script

5. **Verify** the changes:
   ```sql
   DESCRIBE evidence;
   ```

### Expected Output:
```
+-------------+---------------+------+-----+-------------------+
| Field       | Type          | Null | Key | Default           |
+-------------+---------------+------+-----+-------------------+
| id          | bigint        | NO   | PRI | NULL              |
| milestone_id| bigint        | NO   | MUL | NULL              |
| description | text          | YES  |     | NULL              |
| file_path   | varchar(1000) | YES  |     | NULL              |
| created_at  | timestamp     | YES  |     | CURRENT_TIMESTAMP |
| updated_at  | timestamp     | YES  |     | CURRENT_TIMESTAMP |
+-------------+---------------+------+-----+-------------------+
```

## Testing the Feature

### Test Case 1: Submit Evidence
1. Login as campaigner
2. Create escrow campaign with milestone
3. Navigate to Submit Milestone
4. Select milestone
5. Click "Choose Image" and select a .jpg file
6. Verify file name appears in text field
7. Add 1-2 more images (optional)
8. Write completion description
9. Submit
10. ✅ Success message should appear

### Test Case 2: View Evidence
1. Login as donor (who donated to the campaign)
2. Navigate to Voting Requests
3. Click on the submitted milestone
4. ✅ Evidence list should show file names
5. ✅ Evidence details should show paths and timestamps
6. Cast vote

### Test Case 3: Validation
1. Try submitting without images → ❌ Should show warning
2. Try selecting non-image file → ❌ Should reject
3. Try submitting without description → ❌ Should show warning

## Future Enhancements

### Recommended Improvements:
1. **File Upload to Server**: 
   - Store files in `/uploads/evidence/` directory
   - Generate unique filenames to prevent conflicts
   - Implement file size limits (e.g., 5MB per image)

2. **Image Preview**:
   - Add ImageView component to show thumbnails
   - Allow donors to click to view full-size images
   - Implement image carousel for multiple images

3. **Cloud Storage Integration**:
   - Upload to AWS S3, Azure Blob, or similar
   - Store cloud URLs instead of local paths
   - Enable access from any device

4. **Enhanced Validation**:
   - Actual file content validation (not just extension)
   - Image dimension checks
   - Virus scanning for security

5. **Compression**:
   - Auto-compress large images
   - Generate thumbnails for faster loading
   - Optimize for web display

6. **Download Feature**:
   - Allow donors to download evidence
   - Zip multiple files for bulk download

## Files Modified

### Java Files:
- ✅ `SubmitMilestoneController.java` - Image selection and upload
- ✅ `VotingRequestsController.java` - Evidence display
- ✅ `MilestoneService.java` - Evidence processing
- ✅ `EvidenceRepository.java` (NEW) - Repository interface
- ✅ `MySQLEvidenceRepository.java` (NEW) - Database operations

### FXML Files:
- ✅ `submit_milestone.fxml` - FileChooser UI
- ✅ `voting_requests.fxml` - Evidence display UI

### SQL Files:
- ✅ `migration_add_evidence_images.sql` (NEW) - Database migration

### Documentation:
- ✅ `EVIDENCE_UPLOAD_IMPLEMENTATION.md` (this file)

## Troubleshooting

### Issue: Images not showing in voting screen
- Check database: `SELECT * FROM evidence WHERE milestone_id = X;`
- Verify file paths are correct and files exist
- Check console logs for errors

### Issue: File chooser not opening
- Ensure JavaFX FileChooser is properly imported
- Check viewLoader.getPrimaryStage() is not null
- Verify file extension filters are set correctly

### Issue: Migration fails
- Check if table already exists with old structure
- Ensure MySQL user has ALTER privileges
- Run DESCRIBE evidence to see current structure

### Issue: Database errors on submit
- Check foreign key constraint (milestone must exist)
- Verify DBConnection is working
- Check logs for SQL exceptions

## Support

For issues or questions:
1. Check application logs in console
2. Review database logs
3. Verify all files are compiled correctly
4. Ensure migration was run successfully

## Version History

- **v1.0.0** (2025-12-01): Initial implementation
  - FileChooser-based image selection
  - Evidence repository layer
  - Voting screen evidence display
  - Database migration script
