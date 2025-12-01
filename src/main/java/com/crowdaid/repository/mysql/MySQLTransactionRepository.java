package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.donation.Transaction;
import com.crowdaid.model.donation.TransactionStatus;
import com.crowdaid.model.donation.TransactionType;
import com.crowdaid.repository.interfaces.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of TransactionRepository.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLTransactionRepository implements TransactionRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLTransactionRepository.class);
    
    @Override
    public Transaction findById(Long id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTransaction(rs);
            }
            return null;
        }
    }
    
    @Override
    public List<Transaction> findByCampaign(Long campaignId) throws SQLException {
        // Note: The schema doesn't have campaign_id in transactions table
        // This implementation assumes we need to find transactions through donations
        String sql = "SELECT DISTINCT t.* FROM transactions t " +
                     "INNER JOIN donations d ON t.id = d.transaction_id " +
                     "WHERE d.campaign_id = ? ORDER BY t.transaction_date DESC";
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaignId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            return transactions;
        }
    }
    
    @Override
    public List<Transaction> findByDonor(Long donorId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE donor_id = ? ORDER BY transaction_date DESC";
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, donorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            return transactions;
        }
    }
    
    @Override
    public List<Transaction> findByType(TransactionType type) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE type = ? ORDER BY transaction_date DESC";
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            return transactions;
        }
    }
    
    @Override
    public Transaction save(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (escrow_id, campaign_id, donor_id, amount, " +
                     "type, status, reference, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // escrow_id - optional
            if (transaction.getEscrowId() != null) {
                stmt.setLong(1, transaction.getEscrowId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            
            // campaign_id - required
            if (transaction.getCampaignId() != null) {
                stmt.setLong(2, transaction.getCampaignId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            
            // donor_id - optional
            if (transaction.getDonorId() != null) {
                stmt.setLong(3, transaction.getDonorId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }
            
            stmt.setDouble(4, transaction.getAmount());
            stmt.setString(5, transaction.getType().name());
            stmt.setString(6, transaction.getStatus().name());
            stmt.setString(7, transaction.getReference());
            stmt.setString(8, transaction.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
            
            logger.info("Transaction created: id={}, type={}, amount={}", 
                       transaction.getId(), transaction.getType(), transaction.getAmount());
            return transaction;
        }
    }
    
    @Override
    public void update(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET donor_id = ?, amount = ?, type = ?, status = ?, " +
                     "payment_method = ?, reference = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (transaction.getDonorId() != null) {
                stmt.setLong(1, transaction.getDonorId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, transaction.getType().name());
            stmt.setString(4, transaction.getStatus().name());
            stmt.setNull(5, Types.VARCHAR); // payment_method
            stmt.setString(6, transaction.getReference());
            stmt.setLong(7, transaction.getId());
            
            stmt.executeUpdate();
            logger.info("Transaction updated: id={}, status={}", transaction.getId(), transaction.getStatus());
        }
    }
    
    /**
     * Maps a ResultSet row to a Transaction object.
     * 
     * @param rs the ResultSet
     * @return the Transaction object
     * @throws SQLException if database error occurs
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        
        long donorId = rs.getLong("donor_id");
        if (!rs.wasNull()) {
            transaction.setDonorId(donorId);
        }
        
        transaction.setAmount(rs.getDouble("amount"));
        transaction.setType(TransactionType.valueOf(rs.getString("type")));
        transaction.setStatus(TransactionStatus.valueOf(rs.getString("status")));
        transaction.setReference(rs.getString("reference"));
        
        Timestamp transactionDate = rs.getTimestamp("transaction_date");
        if (transactionDate != null) {
            transaction.setCreatedAt(transactionDate.toLocalDateTime());
            transaction.setUpdatedAt(transactionDate.toLocalDateTime());
        }
        
        // Set description from reference or payment_method
        String paymentMethod = rs.getString("payment_method");
        if (paymentMethod != null) {
            transaction.setDescription(paymentMethod);
        }
        
        return transaction;
    }
}
