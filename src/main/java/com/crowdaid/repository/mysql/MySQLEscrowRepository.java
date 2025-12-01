package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.donation.EscrowAccount;
import com.crowdaid.repository.interfaces.EscrowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * MySQL implementation of EscrowRepository.
 * Handles EscrowAccount entity persistence operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLEscrowRepository implements EscrowRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLEscrowRepository.class);
    
    @Override
    public EscrowAccount findById(Long id) throws SQLException {
        String query = "SELECT * FROM escrow_accounts WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEscrowAccount(rs);
                }
            }
        }
        
        return null;
    }
    
    @Override
    public EscrowAccount findByCampaign(Long campaignId) throws SQLException {
        String query = "SELECT * FROM escrow_accounts WHERE campaign_id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, campaignId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEscrowAccount(rs);
                }
            }
        }
        
        return null;
    }
    
    @Override
    public EscrowAccount save(EscrowAccount escrowAccount) throws SQLException {
        String query = "INSERT INTO escrow_accounts (campaign_id, total_amount, available_amount, released_amount) " +
                      "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, escrowAccount.getCampaignId());
            stmt.setDouble(2, escrowAccount.getTotalAmount());
            stmt.setDouble(3, escrowAccount.getAvailableAmount());
            stmt.setDouble(4, escrowAccount.getReleasedAmount());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating escrow account failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    escrowAccount.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating escrow account failed, no ID obtained.");
                }
            }
            
            logger.info("Escrow account saved successfully: id={}, campaignId={}", 
                       escrowAccount.getId(), escrowAccount.getCampaignId());
            
            return escrowAccount;
        }
    }
    
    @Override
    public void update(EscrowAccount escrowAccount) throws SQLException {
        String query = "UPDATE escrow_accounts SET campaign_id = ?, total_amount = ?, " +
                      "available_amount = ?, released_amount = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, escrowAccount.getCampaignId());
            stmt.setDouble(2, escrowAccount.getTotalAmount());
            stmt.setDouble(3, escrowAccount.getAvailableAmount());
            stmt.setDouble(4, escrowAccount.getReleasedAmount());
            stmt.setLong(5, escrowAccount.getId());
            
            stmt.executeUpdate();
            
            logger.info("Escrow account updated successfully: id={}", escrowAccount.getId());
        }
    }
    
    @Override
    public void addFunds(Long escrowId, double amount) throws SQLException {
        String query = "UPDATE escrow_accounts SET total_amount = total_amount + ?, " +
                      "available_amount = available_amount + ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDouble(1, amount);
            stmt.setDouble(2, amount);
            stmt.setLong(3, escrowId);
            
            stmt.executeUpdate();
            
            logger.info("Funds added to escrow account: escrowId={}, amount={}", escrowId, amount);
        }
    }
    
    @Override
    public boolean releaseFunds(Long escrowId, double amount) throws SQLException {
        String query = "UPDATE escrow_accounts SET available_amount = available_amount - ?, " +
                      "released_amount = released_amount + ? WHERE id = ? AND available_amount >= ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDouble(1, amount);
            stmt.setDouble(2, amount);
            stmt.setLong(3, escrowId);
            stmt.setDouble(4, amount);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Funds released from escrow account: escrowId={}, amount={}", escrowId, amount);
                return true;
            } else {
                logger.warn("Failed to release funds: insufficient balance in escrow account: escrowId={}", escrowId);
                return false;
            }
        }
    }
    
    private EscrowAccount mapResultSetToEscrowAccount(ResultSet rs) throws SQLException {
        EscrowAccount account = new EscrowAccount();
        account.setId(rs.getLong("id"));
        account.setCampaignId(rs.getLong("campaign_id"));
        account.setTotalAmount(rs.getDouble("total_amount"));
        account.setAvailableAmount(rs.getDouble("available_amount"));
        account.setReleasedAmount(rs.getDouble("released_amount"));
        return account;
    }
}
