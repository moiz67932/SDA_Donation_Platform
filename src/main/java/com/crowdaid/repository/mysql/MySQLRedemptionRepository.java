package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.reward.Redemption;
import com.crowdaid.model.reward.RedemptionStatus;
import com.crowdaid.repository.interfaces.RedemptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of RedemptionRepository.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLRedemptionRepository implements RedemptionRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLRedemptionRepository.class);
    
    @Override
    public Redemption findById(Long id) throws SQLException {
        String sql = "SELECT * FROM redemptions WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRedemption(rs);
            }
            return null;
        }
    }
    
    @Override
    public List<Redemption> findByDonor(Long donorId) throws SQLException {
        String sql = "SELECT * FROM redemptions WHERE donor_id = ? ORDER BY redemption_date DESC";
        List<Redemption> redemptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, donorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                redemptions.add(mapResultSetToRedemption(rs));
            }
            return redemptions;
        }
    }
    
    @Override
    public List<Redemption> findByReward(Long rewardId) throws SQLException {
        String sql = "SELECT * FROM redemptions WHERE reward_id = ? ORDER BY redemption_date DESC";
        List<Redemption> redemptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, rewardId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                redemptions.add(mapResultSetToRedemption(rs));
            }
            return redemptions;
        }
    }
    
    @Override
    public List<Redemption> findByStatus(RedemptionStatus status) throws SQLException {
        String sql = "SELECT * FROM redemptions WHERE status = ? ORDER BY redemption_date DESC";
        List<Redemption> redemptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                redemptions.add(mapResultSetToRedemption(rs));
            }
            return redemptions;
        }
    }
    
    @Override
    public Redemption save(Redemption redemption) throws SQLException {
        String sql = "INSERT INTO redemptions (donor_id, reward_id, credits_spent, status, redemption_date) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, redemption.getDonorId());
            stmt.setLong(2, redemption.getRewardId());
            stmt.setDouble(3, redemption.getCreditsSpent());
            stmt.setString(4, redemption.getStatus().name());
            stmt.setTimestamp(5, Timestamp.valueOf(redemption.getCreatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating redemption failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    redemption.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating redemption failed, no ID obtained.");
                }
            }
            
            logger.info("Redemption created: id={}, donorId={}, rewardId={}", 
                       redemption.getId(), redemption.getDonorId(), redemption.getRewardId());
            return redemption;
        }
    }
    
    @Override
    public void update(Redemption redemption) throws SQLException {
        String sql = "UPDATE redemptions SET donor_id = ?, reward_id = ?, credits_spent = ?, status = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, redemption.getDonorId());
            stmt.setLong(2, redemption.getRewardId());
            stmt.setDouble(3, redemption.getCreditsSpent());
            stmt.setString(4, redemption.getStatus().name());
            stmt.setLong(5, redemption.getId());
            
            stmt.executeUpdate();
            logger.info("Redemption updated: id={}, status={}", redemption.getId(), redemption.getStatus());
        }
    }
    
    /**
     * Maps a ResultSet row to a Redemption object.
     * 
     * @param rs the ResultSet
     * @return the Redemption object
     * @throws SQLException if database error occurs
     */
    private Redemption mapResultSetToRedemption(ResultSet rs) throws SQLException {
        Redemption redemption = new Redemption();
        redemption.setId(rs.getLong("id"));
        redemption.setDonorId(rs.getLong("donor_id"));
        redemption.setRewardId(rs.getLong("reward_id"));
        redemption.setCreditsSpent(rs.getDouble("credits_used"));
        redemption.setStatus(RedemptionStatus.valueOf(rs.getString("status")));
        redemption.setCreatedAt(rs.getTimestamp("redemption_date").toLocalDateTime());
        redemption.setUpdatedAt(rs.getTimestamp("redemption_date").toLocalDateTime());
        
        return redemption;
    }
}
