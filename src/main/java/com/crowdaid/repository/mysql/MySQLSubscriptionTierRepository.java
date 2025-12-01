package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.donation.SubscriptionTier;
import com.crowdaid.repository.interfaces.SubscriptionTierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of SubscriptionTierRepository.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLSubscriptionTierRepository implements SubscriptionTierRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLSubscriptionTierRepository.class);
    
    @Override
    public SubscriptionTier findById(Long id) throws SQLException {
        String sql = "SELECT * FROM subscription_tiers WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTier(rs);
            }
            return null;
        }
    }
    
    @Override
    public List<SubscriptionTier> findByCampaign(Long campaignId) throws SQLException {
        String sql = "SELECT * FROM subscription_tiers WHERE campaign_id = ? ORDER BY monthly_amount ASC";
        List<SubscriptionTier> tiers = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaignId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tiers.add(mapResultSetToTier(rs));
            }
            return tiers;
        }
    }
    
    @Override
    public SubscriptionTier findByCampaignAndName(Long campaignId, String tierName) throws SQLException {
        String sql = "SELECT * FROM subscription_tiers WHERE campaign_id = ? AND tier_name = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaignId);
            stmt.setString(2, tierName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTier(rs);
            }
            return null;
        }
    }
    
    @Override
    public SubscriptionTier save(SubscriptionTier tier) throws SQLException {
        String sql = "INSERT INTO subscription_tiers (campaign_id, tier_name, monthly_amount, description, benefits, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, tier.getCampaignId());
            stmt.setString(2, tier.getTierName());
            stmt.setDouble(3, tier.getMonthlyAmount());
            stmt.setString(4, tier.getDescription());
            stmt.setString(5, tier.getBenefits());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating subscription tier failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tier.setId(generatedKeys.getLong(1));
                    logger.info("Created subscription tier with ID: {}", tier.getId());
                } else {
                    throw new SQLException("Creating subscription tier failed, no ID obtained.");
                }
            }
            
            return tier;
        }
    }
    
    @Override
    public void update(SubscriptionTier tier) throws SQLException {
        String sql = "UPDATE subscription_tiers SET tier_name = ?, monthly_amount = ?, description = ?, " +
                     "benefits = ?, updated_at = NOW() WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tier.getTierName());
            stmt.setDouble(2, tier.getMonthlyAmount());
            stmt.setString(3, tier.getDescription());
            stmt.setString(4, tier.getBenefits());
            stmt.setLong(5, tier.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating subscription tier failed, no rows affected.");
            }
            
            logger.info("Updated subscription tier with ID: {}", tier.getId());
        }
    }
    
    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM subscription_tiers WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Deleting subscription tier failed, no rows affected.");
            }
            
            logger.info("Deleted subscription tier with ID: {}", id);
        }
    }
    
    @Override
    public int countActiveSubscriptions(Long tierId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM subscriptions WHERE tier_id = ? AND status = 'ACTIVE'";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, tierId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    /**
     * Maps ResultSet to SubscriptionTier object.
     * 
     * @param rs the ResultSet
     * @return the SubscriptionTier
     * @throws SQLException if error occurs
     */
    private SubscriptionTier mapResultSetToTier(ResultSet rs) throws SQLException {
        SubscriptionTier tier = new SubscriptionTier();
        tier.setId(rs.getLong("id"));
        tier.setCampaignId(rs.getLong("campaign_id"));
        tier.setTierName(rs.getString("tier_name"));
        tier.setMonthlyAmount(rs.getDouble("monthly_amount"));
        tier.setDescription(rs.getString("description"));
        tier.setBenefits(rs.getString("benefits"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            tier.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            tier.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return tier;
    }
}
