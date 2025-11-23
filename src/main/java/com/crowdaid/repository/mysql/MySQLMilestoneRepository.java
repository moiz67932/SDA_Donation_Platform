package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.campaign.Milestone;
import com.crowdaid.model.campaign.MilestoneStatus;
import com.crowdaid.repository.interfaces.MilestoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of MilestoneRepository.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLMilestoneRepository implements MilestoneRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLMilestoneRepository.class);
    
    @Override
    public Milestone findById(Long id) throws SQLException {
        String sql = "SELECT * FROM milestones WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToMilestone(rs);
            }
            return null;
        }
    }
    
    @Override
    public List<Milestone> findByCampaign(Long campaignId) throws SQLException {
        String sql = "SELECT * FROM milestones WHERE campaign_id = ? ORDER BY expected_date ASC";
        List<Milestone> milestones = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaignId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                milestones.add(mapResultSetToMilestone(rs));
            }
            return milestones;
        }
    }
    
    @Override
    public List<Milestone> findByStatus(MilestoneStatus status) throws SQLException {
        String sql = "SELECT * FROM milestones WHERE status = ? ORDER BY created_at DESC";
        List<Milestone> milestones = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                milestones.add(mapResultSetToMilestone(rs));
            }
            return milestones;
        }
    }
    
    @Override
    public List<Milestone> findUnderReview() throws SQLException {
        return findByStatus(MilestoneStatus.UNDER_REVIEW);
    }
    
    @Override
    public Milestone save(Milestone milestone) throws SQLException {
        String sql = "INSERT INTO milestones (campaign_id, title, description, amount, expected_date, " +
                     "status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, milestone.getCampaignId());
            stmt.setString(2, milestone.getTitle());
            stmt.setString(3, milestone.getDescription());
            stmt.setDouble(4, milestone.getAmount());
            stmt.setDate(5, milestone.getExpectedDate() != null ? 
                         Date.valueOf(milestone.getExpectedDate()) : null);
            stmt.setString(6, milestone.getStatus().name());
            stmt.setTimestamp(7, Timestamp.valueOf(milestone.getCreatedAt()));
            stmt.setTimestamp(8, Timestamp.valueOf(milestone.getUpdatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating milestone failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    milestone.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating milestone failed, no ID obtained.");
                }
            }
            
            logger.info("Milestone created: id={}, title={}, campaignId={}", 
                       milestone.getId(), milestone.getTitle(), milestone.getCampaignId());
            return milestone;
        }
    }
    
    @Override
    public void update(Milestone milestone) throws SQLException {
        String sql = "UPDATE milestones SET campaign_id = ?, title = ?, description = ?, " +
                     "amount = ?, expected_date = ?, status = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, milestone.getCampaignId());
            stmt.setString(2, milestone.getTitle());
            stmt.setString(3, milestone.getDescription());
            stmt.setDouble(4, milestone.getAmount());
            stmt.setDate(5, milestone.getExpectedDate() != null ? 
                         Date.valueOf(milestone.getExpectedDate()) : null);
            stmt.setString(6, milestone.getStatus().name());
            stmt.setTimestamp(7, Timestamp.valueOf(milestone.getUpdatedAt()));
            stmt.setLong(8, milestone.getId());
            
            stmt.executeUpdate();
            logger.info("Milestone updated: id={}", milestone.getId());
        }
    }
    
    @Override
    public void updateStatus(Long milestoneId, MilestoneStatus newStatus) throws SQLException {
        String sql = "UPDATE milestones SET status = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatus.name());
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setLong(3, milestoneId);
            
            stmt.executeUpdate();
            logger.info("Milestone status updated: id={}, newStatus={}", milestoneId, newStatus);
        }
    }
    
    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM milestones WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Milestone deleted: id={}", id);
            } else {
                logger.warn("No milestone found with id={}", id);
            }
        }
    }
    
    /**
     * Maps a ResultSet row to a Milestone object.
     * 
     * @param rs the ResultSet
     * @return the Milestone object
     * @throws SQLException if database error occurs
     */
    private Milestone mapResultSetToMilestone(ResultSet rs) throws SQLException {
        Milestone milestone = new Milestone();
        
        milestone.setId(rs.getLong("id"));
        milestone.setCampaignId(rs.getLong("campaign_id"));
        milestone.setTitle(rs.getString("title"));
        milestone.setDescription(rs.getString("description"));
        milestone.setAmount(rs.getDouble("amount"));
        
        Date expectedDate = rs.getDate("expected_date");
        if (expectedDate != null) {
            milestone.setExpectedDate(expectedDate.toLocalDate());
        }
        
        milestone.setStatus(MilestoneStatus.valueOf(rs.getString("status")));
        milestone.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        milestone.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        return milestone;
    }
}
