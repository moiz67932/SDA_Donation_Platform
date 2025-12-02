package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.donation.Subscription;
import com.crowdaid.model.donation.SubscriptionStatus;
import com.crowdaid.repository.interfaces.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of SubscriptionRepository.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLSubscriptionRepository implements SubscriptionRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLSubscriptionRepository.class);
    
    @Override
    public Subscription findById(Long id) throws SQLException {
        String sql = "SELECT s.*, st.tier_name, st.description as tier_description, st.benefits " +
                     "FROM subscriptions s " +
                     "LEFT JOIN subscription_tiers st ON s.tier_id = st.id " +
                     "WHERE s.id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSubscription(rs);
            }
            return null;
        }
    }
    
    @Override
    public List<Subscription> findByCampaign(Long campaignId) throws SQLException {
        String sql = "SELECT s.*, st.tier_name, st.description as tier_description, st.benefits " +
                     "FROM subscriptions s " +
                     "LEFT JOIN subscription_tiers st ON s.tier_id = st.id " +
                     "WHERE s.campaign_id = ? ORDER BY s.start_date DESC";
        List<Subscription> subscriptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaignId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                subscriptions.add(mapResultSetToSubscription(rs));
            }
            return subscriptions;
        }
    }
    
    @Override
    public List<Subscription> findByDonor(Long donorId) throws SQLException {
        String sql = "SELECT s.*, st.tier_name, st.description as tier_description, st.benefits " +
                     "FROM subscriptions s " +
                     "LEFT JOIN subscription_tiers st ON s.tier_id = st.id " +
                     "WHERE s.donor_id = ? ORDER BY s.start_date DESC";
        List<Subscription> subscriptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, donorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                subscriptions.add(mapResultSetToSubscription(rs));
            }
            return subscriptions;
        }
    }
    
    @Override
    public List<Subscription> findActiveByDonor(Long donorId) throws SQLException {
        String sql = "SELECT s.*, st.tier_name, st.description as tier_description, st.benefits " +
                     "FROM subscriptions s " +
                     "LEFT JOIN subscription_tiers st ON s.tier_id = st.id " +
                     "WHERE s.donor_id = ? AND s.status = 'ACTIVE' ORDER BY s.start_date DESC";
        List<Subscription> subscriptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, donorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                subscriptions.add(mapResultSetToSubscription(rs));
            }
            return subscriptions;
        }
    }
    
    @Override
    public boolean hasActiveSubscription(Long donorId, Long campaignId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM subscriptions " +
                     "WHERE donor_id = ? AND campaign_id = ? AND status = 'ACTIVE'";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, donorId);
            stmt.setLong(2, campaignId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }
    
    @Override
    public Subscription save(Subscription subscription) throws SQLException {
        String sql = "INSERT INTO subscriptions (campaign_id, donor_id, tier_id, tier_name, monthly_amount, " +
                     "status, start_date, next_billing_date, description, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, subscription.getCampaignId());
            stmt.setLong(2, subscription.getDonorId());
            
            if (subscription.getTierId() != null) {
                stmt.setLong(3, subscription.getTierId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }
            
            stmt.setString(4, subscription.getTierName());
            stmt.setDouble(5, subscription.getMonthlyAmount());
            stmt.setString(6, subscription.getStatus().name());
            stmt.setDate(7, Date.valueOf(subscription.getStartDate()));
            stmt.setDate(8, Date.valueOf(subscription.getNextBillingDate()));
            stmt.setString(9, subscription.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating subscription failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    subscription.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating subscription failed, no ID obtained.");
                }
            }
            
            logger.info("Subscription created: id={}, donorId={}, campaignId={}, tierId={}", 
                       subscription.getId(), subscription.getDonorId(), subscription.getCampaignId(), subscription.getTierId());
            return subscription;
        }
    }
    
    @Override
    public void update(Subscription subscription) throws SQLException {
        String sql = "UPDATE subscriptions SET tier_id = ?, tier_name = ?, monthly_amount = ?, " +
                     "status = ?, next_billing_date = ?, cancel_date = ?, description = ?, updated_at = NOW() " +
                     "WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (subscription.getTierId() != null) {
                stmt.setLong(1, subscription.getTierId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            
            stmt.setString(2, subscription.getTierName());
            stmt.setDouble(3, subscription.getMonthlyAmount());
            stmt.setString(4, subscription.getStatus().name());
            stmt.setDate(5, Date.valueOf(subscription.getNextBillingDate()));
            
            if (subscription.getCancelDate() != null) {
                stmt.setDate(6, Date.valueOf(subscription.getCancelDate()));
            } else {
                stmt.setNull(6, Types.DATE);
            }
            
            stmt.setString(7, subscription.getDescription());
            stmt.setLong(8, subscription.getId());
            
            stmt.executeUpdate();
            logger.info("Subscription updated: id={}, status={}", subscription.getId(), subscription.getStatus());
        }
    }
    
    @Override
    public void updateStatus(Long subscriptionId, SubscriptionStatus newStatus) throws SQLException {
        String sql = "UPDATE subscriptions SET status = ?, updated_at = NOW() WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatus.name());
            stmt.setLong(2, subscriptionId);
            
            stmt.executeUpdate();
            logger.info("Subscription status updated: id={}, status={}", subscriptionId, newStatus);
        }
    }
    
    /**
     * Maps a ResultSet row to a Subscription object.
     * 
     * @param rs the ResultSet
     * @return the Subscription object
     * @throws SQLException if database error occurs
     */
    private Subscription mapResultSetToSubscription(ResultSet rs) throws SQLException {
        Subscription subscription = new Subscription();
        subscription.setId(rs.getLong("id"));
        subscription.setCampaignId(rs.getLong("campaign_id"));
        subscription.setDonorId(rs.getLong("donor_id"));
        
        Long tierId = rs.getLong("tier_id");
        if (!rs.wasNull()) {
            subscription.setTierId(tierId);
        }
        
        // Get tier_name from subscriptions table or join with subscription_tiers
        String tierName = rs.getString("tier_name");
        subscription.setTierName(tierName != null ? tierName : "Unknown Tier");
        
        subscription.setMonthlyAmount(rs.getDouble("monthly_amount"));
        
        // Safely parse status enum
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                subscription.setStatus(SubscriptionStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid subscription status: {}, defaulting to ACTIVE", statusStr);
                subscription.setStatus(SubscriptionStatus.ACTIVE);
            }
        } else {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }
        
        subscription.setDescription(rs.getString("description"));
        
        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            subscription.setStartDate(startDate.toLocalDate());
        }
        
        Date nextBillingDate = rs.getDate("next_billing_date");
        if (nextBillingDate != null) {
            subscription.setNextBillingDate(nextBillingDate.toLocalDate());
        }
        
        Date cancelDate = rs.getDate("cancel_date");
        if (cancelDate != null && !rs.wasNull()) {
            subscription.setCancelDate(cancelDate.toLocalDate());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            subscription.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            subscription.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return subscription;
    }
}
