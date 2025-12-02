package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.campaign.Evidence;
import com.crowdaid.repository.interfaces.EvidenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of EvidenceRepository.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLEvidenceRepository implements EvidenceRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLEvidenceRepository.class);
    
    @Override
    public Evidence save(Evidence evidence) throws SQLException {
        String sql = "INSERT INTO evidence (milestone_id, description, file_path, created_at) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, evidence.getMilestoneId());
            stmt.setString(2, evidence.getDescription());
            stmt.setString(3, evidence.getFilePath());
            stmt.setTimestamp(4, Timestamp.valueOf(evidence.getCreatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating evidence failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    evidence.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating evidence failed, no ID obtained.");
                }
            }
            
            logger.info("Evidence created: id={}, milestoneId={}, filePath={}", 
                       evidence.getId(), evidence.getMilestoneId(), evidence.getFilePath());
            return evidence;
        }
    }
    
    @Override
    public Evidence findById(Long id) throws SQLException {
        String sql = "SELECT * FROM evidence WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEvidence(rs);
            }
            return null;
        }
    }
    
    @Override
    public List<Evidence> findByMilestone(Long milestoneId) throws SQLException {
        String sql = "SELECT * FROM evidence WHERE milestone_id = ? ORDER BY created_at ASC";
        List<Evidence> evidenceList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, milestoneId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                evidenceList.add(mapResultSetToEvidence(rs));
            }
            
            logger.debug("Found {} evidence items for milestone {}", evidenceList.size(), milestoneId);
            return evidenceList;
        }
    }
    
    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM evidence WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Evidence deleted: id={}", id);
            } else {
                logger.warn("No evidence found with id={}", id);
            }
        }
    }
    
    @Override
    public void deleteByMilestone(Long milestoneId) throws SQLException {
        String sql = "DELETE FROM evidence WHERE milestone_id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, milestoneId);
            int affectedRows = stmt.executeUpdate();
            
            logger.info("Deleted {} evidence items for milestone {}", affectedRows, milestoneId);
        }
    }
    
    /**
     * Maps a ResultSet row to an Evidence object.
     * 
     * @param rs the ResultSet
     * @return the Evidence object
     * @throws SQLException if database error occurs
     */
    private Evidence mapResultSetToEvidence(ResultSet rs) throws SQLException {
        Evidence evidence = new Evidence();
        
        evidence.setId(rs.getLong("id"));
        evidence.setMilestoneId(rs.getLong("milestone_id"));
        evidence.setDescription(rs.getString("description"));
        evidence.setFilePath(rs.getString("file_path"));
        evidence.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            evidence.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return evidence;
    }
}
