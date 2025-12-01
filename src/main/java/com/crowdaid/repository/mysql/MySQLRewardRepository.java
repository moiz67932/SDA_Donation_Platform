package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.reward.Reward;
import com.crowdaid.model.reward.RewardCategory;
import com.crowdaid.model.reward.RewardStatus;
import com.crowdaid.repository.interfaces.RewardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of RewardRepository.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLRewardRepository implements RewardRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLRewardRepository.class);
    
    @Override
    public Reward findById(Long id) throws SQLException {
        String sql = "SELECT * FROM rewards WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToReward(rs);
            }
            return null;
        }
    }
    
    @Override
    public List<Reward> findAllAvailable() throws SQLException {
        String sql = "SELECT * FROM rewards WHERE status = 'AVAILABLE' AND stock > 0";
        List<Reward> rewards = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                rewards.add(mapResultSetToReward(rs));
            }
            return rewards;
        }
    }
    
    @Override
    public List<Reward> findAll() throws SQLException {
        String sql = "SELECT * FROM rewards";
        List<Reward> rewards = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                rewards.add(mapResultSetToReward(rs));
            }
            return rewards;
        }
    }
    
    @Override
    public List<Reward> findByCategory(RewardCategory category) throws SQLException {
        String sql = "SELECT * FROM rewards WHERE category = ?";
        List<Reward> rewards = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                rewards.add(mapResultSetToReward(rs));
            }
            return rewards;
        }
    }
    
    @Override
    public List<Reward> findByStatus(RewardStatus status) throws SQLException {
        String sql;
        if (status == RewardStatus.AVAILABLE) {
            sql = "SELECT * FROM rewards WHERE status = 'AVAILABLE'";
        } else if (status == RewardStatus.OUT_OF_STOCK) {
            sql = "SELECT * FROM rewards WHERE status = 'OUT_OF_STOCK'";
        } else { // DISABLED
            sql = "SELECT * FROM rewards WHERE status = 'DISABLED'";
        }
        
        List<Reward> rewards = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                rewards.add(mapResultSetToReward(rs));
            }
            return rewards;
        }
    }
    
    @Override
    public Reward save(Reward reward) throws SQLException {
        String sql = "INSERT INTO rewards (name, description, credit_cost, category, stock, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, reward.getName());
            stmt.setString(2, reward.getDescription());
            stmt.setDouble(3, reward.getCreditCost());
            stmt.setString(4, reward.getCategory().name());
            stmt.setInt(5, reward.getStock());
            stmt.setString(6, reward.getStatus().name());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating reward failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reward.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating reward failed, no ID obtained.");
                }
            }
            
            logger.info("Reward created: id={}, name={}", reward.getId(), reward.getName());
            return reward;
        }
    }
    
    @Override
    public void update(Reward reward) throws SQLException {
        String sql = "UPDATE rewards SET name = ?, description = ?, credit_cost = ?, category = ?, " +
                     "stock = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, reward.getName());
            stmt.setString(2, reward.getDescription());
            stmt.setDouble(3, reward.getCreditCost());
            stmt.setString(4, reward.getCategory().name());
            stmt.setInt(5, reward.getStock());
            stmt.setString(6, reward.getStatus().name());
            stmt.setLong(7, reward.getId());
            
            stmt.executeUpdate();
            logger.info("Reward updated: id={}", reward.getId());
        }
    }
    
    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM rewards WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            logger.info("Reward deleted: id={}", id);
        }
    }
    
    @Override
    public boolean decrementStock(Long rewardId) throws SQLException {
        return decreaseStock(rewardId, 1);
    }
    
    @Override
    public boolean decreaseStock(Long rewardId, int quantity) throws SQLException {
        String sql = "UPDATE rewards SET stock = stock - 1 " +
                     "WHERE id = ? AND stock > 0";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, rewardId);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Reward stock decremented: id={}", rewardId);
                return true;
            }
            logger.warn("Failed to decrement stock for reward: id={} (out of stock)", rewardId);
            return false;
        }
    }
    
    /**
     * Maps a ResultSet row to a Reward object.
     * 
     * @param rs the ResultSet
     * @return the Reward object
     * @throws SQLException if database error occurs
     */
    private Reward mapResultSetToReward(ResultSet rs) throws SQLException {
        Reward reward = new Reward();
        reward.setId(rs.getLong("id"));
        reward.setName(rs.getString("name"));
        reward.setDescription(rs.getString("description"));
        reward.setCreditCost(rs.getDouble("credit_cost"));
        reward.setCategory(RewardCategory.valueOf(rs.getString("category")));
        reward.setStock(rs.getInt("stock"));
        reward.setStatus(RewardStatus.valueOf(rs.getString("status")));
        
        return reward;
    }
}
