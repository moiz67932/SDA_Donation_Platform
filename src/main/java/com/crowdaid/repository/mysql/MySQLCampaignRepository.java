package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.CampaignCategory;
import com.crowdaid.model.campaign.CampaignStatus;
import com.crowdaid.repository.interfaces.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of CampaignRepository.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLCampaignRepository implements CampaignRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLCampaignRepository.class);
    
    @Override
    public Campaign findById(Long id) throws SQLException {
        String sql = "SELECT * FROM campaigns WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCampaign(rs);
            }
            return null;
        }
    }
    
    @Override
    public List<Campaign> findAllActive() throws SQLException {
        String sql = "SELECT * FROM campaigns WHERE status = 'ACTIVE' ORDER BY created_at DESC";
        List<Campaign> campaigns = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                campaigns.add(mapResultSetToCampaign(rs));
            }
            return campaigns;
        }
    }
    
    @Override
    public List<Campaign> findByCampaigner(Long campaignerId) throws SQLException {
        return findByCampaignerId(campaignerId);
    }
    
    public List<Campaign> findByCampaignerId(Long campaignerId) throws SQLException {
        String sql = "SELECT * FROM campaigns WHERE campaigner_id = ? ORDER BY created_at DESC";
        List<Campaign> campaigns = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaignerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                campaigns.add(mapResultSetToCampaign(rs));
            }
            return campaigns;
        }
    }
    
    @Override
    public List<Campaign> findByStatus(CampaignStatus status) throws SQLException {
        String sql = "SELECT * FROM campaigns WHERE status = ? ORDER BY created_at DESC";
        List<Campaign> campaigns = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                campaigns.add(mapResultSetToCampaign(rs));
            }
            return campaigns;
        }
    }
    
    @Override
    public List<Campaign> search(String keyword, CampaignCategory category) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM campaigns WHERE status = 'ACTIVE' AND (title LIKE ? OR description LIKE ?)"
        );
        
        if (category != null) {
            sql.append(" AND category = ?");
        }
        
        sql.append(" ORDER BY created_at DESC");
        
        List<Campaign> campaigns = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            if (category != null) {
                stmt.setString(3, category.name());
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                campaigns.add(mapResultSetToCampaign(rs));
            }
            return campaigns;
        }
    }
    
    @Override
    public List<Campaign> findCreditEarningCampaigns() throws SQLException {
        String sql = "SELECT * FROM campaigns WHERE status = 'ACTIVE' AND (is_philanthropic = TRUE OR is_civic = TRUE OR is_reward_eligible = TRUE) " +
                     "ORDER BY created_at DESC";
        List<Campaign> campaigns = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                campaigns.add(mapResultSetToCampaign(rs));
            }
            return campaigns;
        }
    }
    
    @Override
    public Campaign save(Campaign campaign) throws SQLException {
        String sql = "INSERT INTO campaigns (campaigner_id, title, description, goal_amount, collected_amount, " +
                     "category, status, start_date, end_date, is_philanthropic, is_civic, is_escrow_enabled, is_reward_eligible, image_url, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, campaign.getCampaignerId());
            stmt.setString(2, campaign.getTitle());
            stmt.setString(3, campaign.getDescription());
            stmt.setDouble(4, campaign.getGoalAmount());
            stmt.setDouble(5, campaign.getCollectedAmount());
            stmt.setString(6, campaign.getCategory().name());
            stmt.setString(7, campaign.getStatus().name());
            stmt.setDate(8, campaign.getStartDate() != null ? Date.valueOf(campaign.getStartDate()) : null);
            stmt.setDate(9, campaign.getEndDate() != null ? Date.valueOf(campaign.getEndDate()) : null);
            stmt.setBoolean(10, campaign.isPhilanthropic());
            stmt.setBoolean(11, campaign.isCivic());
            stmt.setBoolean(12, campaign.isEscrowEnabled());
            stmt.setBoolean(13, campaign.isRewardEligible());
            stmt.setString(14, campaign.getImageUrl());
            stmt.setTimestamp(15, Timestamp.valueOf(campaign.getCreatedAt()));
            stmt.setTimestamp(16, Timestamp.valueOf(campaign.getUpdatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating campaign failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    campaign.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating campaign failed, no ID obtained.");
                }
            }
            
            logger.info("Campaign created: id={}, title={}", campaign.getId(), campaign.getTitle());
            return campaign;
        }
    }
    
    @Override
    public void update(Campaign campaign) throws SQLException {
        String sql = "UPDATE campaigns SET campaigner_id = ?, title = ?, description = ?, goal_amount = ?, " +
                     "collected_amount = ?, category = ?, status = ?, start_date = ?, end_date = ?, " +
                     "is_philanthropic = ?, is_civic = ?, is_escrow_enabled = ?, is_reward_eligible = ?, image_url = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaign.getCampaignerId());
            stmt.setString(2, campaign.getTitle());
            stmt.setString(3, campaign.getDescription());
            stmt.setDouble(4, campaign.getGoalAmount());
            stmt.setDouble(5, campaign.getCollectedAmount());
            stmt.setString(6, campaign.getCategory().name());
            stmt.setString(7, campaign.getStatus().name());
            stmt.setDate(8, campaign.getStartDate() != null ? Date.valueOf(campaign.getStartDate()) : null);
            stmt.setDate(9, campaign.getEndDate() != null ? Date.valueOf(campaign.getEndDate()) : null);
            stmt.setBoolean(10, campaign.isPhilanthropic());
            stmt.setBoolean(11, campaign.isCivic());
            stmt.setBoolean(12, campaign.isEscrowEnabled());
            stmt.setBoolean(13, campaign.isRewardEligible());
            stmt.setString(14, campaign.getImageUrl());
            stmt.setTimestamp(15, Timestamp.valueOf(campaign.getUpdatedAt()));
            stmt.setLong(16, campaign.getId());
            
            stmt.executeUpdate();
            logger.info("Campaign updated: id={}", campaign.getId());
        }
    }
    
    @Override
    public void updateStatus(Long campaignId, CampaignStatus newStatus) throws SQLException {
        String sql = "UPDATE campaigns SET status = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatus.name());
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setLong(3, campaignId);
            
            stmt.executeUpdate();
            logger.info("Campaign status updated: id={}, newStatus={}", campaignId, newStatus);
        }
    }
    
    @Override
    public void updateCollectedAmount(Long campaignId, double newAmount) throws SQLException {
        String sql = "UPDATE campaigns SET collected_amount = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, newAmount);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setLong(3, campaignId);
            
            stmt.executeUpdate();
            logger.debug("Campaign collected amount updated: id={}, newAmount={}", campaignId, newAmount);
        }
    }
    
    @Override
    public List<Campaign> findByCategory(CampaignCategory category) throws SQLException {
        String sql = "SELECT * FROM campaigns WHERE category = ? ORDER BY created_at DESC";
        List<Campaign> campaigns = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                campaigns.add(mapResultSetToCampaign(rs));
            }
            return campaigns;
        }
    }
    
    @Override
    public List<Campaign> searchByKeyword(String keyword) throws SQLException {
        String sql = "SELECT * FROM campaigns WHERE status = 'ACTIVE' AND (title LIKE ? OR description LIKE ?) " +
                     "ORDER BY created_at DESC";
        List<Campaign> campaigns = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                campaigns.add(mapResultSetToCampaign(rs));
            }
            return campaigns;
        }
    }
    
    /**
     * Maps a ResultSet row to a Campaign object.
     * 
     * @param rs the ResultSet
     * @return the Campaign object
     * @throws SQLException if database error occurs
     */
    private Campaign mapResultSetToCampaign(ResultSet rs) throws SQLException {
        Campaign campaign = new Campaign();
        
        campaign.setId(rs.getLong("id"));
        campaign.setCampaignerId(rs.getLong("campaigner_id"));
        campaign.setTitle(rs.getString("title"));
        campaign.setDescription(rs.getString("description"));
        campaign.setGoalAmount(rs.getDouble("goal_amount"));
        campaign.setCollectedAmount(rs.getDouble("collected_amount"));
        campaign.setCategory(CampaignCategory.valueOf(rs.getString("category")));
        campaign.setStatus(CampaignStatus.valueOf(rs.getString("status")));
        
        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            campaign.setStartDate(startDate.toLocalDate());
        }
        
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            campaign.setEndDate(endDate.toLocalDate());
        }
        
        campaign.setPhilanthropic(rs.getBoolean("is_philanthropic"));
        campaign.setCivic(rs.getBoolean("is_civic"));
        campaign.setImageUrl(rs.getString("image_url"));
        
        // Load escrow_enabled and reward_eligible fields
        try {
            campaign.setEscrowEnabled(rs.getBoolean("is_escrow_enabled"));
        } catch (SQLException e) {
            campaign.setEscrowEnabled(false);
        }
        
        try {
            campaign.setRewardEligible(rs.getBoolean("is_reward_eligible"));
        } catch (SQLException e) {
            campaign.setRewardEligible(false);
        }
        
        campaign.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        campaign.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        return campaign;
    }
    
    @Override
    public int countByStatus(CampaignStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM campaigns WHERE status = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    @Override
    public int countActiveByCampaigner(Long campaignerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM campaigns WHERE campaigner_id = ? AND status = 'ACTIVE'";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaignerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    @Override
    public double getTotalRaisedByCampaigner(Long campaignerId) throws SQLException {
        String sql = "SELECT SUM(collected_amount) FROM campaigns WHERE campaigner_id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaignerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }
}
