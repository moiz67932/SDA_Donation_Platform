package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.user.Credit;
import com.crowdaid.repository.interfaces.CreditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * MySQL implementation of CreditRepository.
 * Handles Credit entity persistence operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLCreditRepository implements CreditRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLCreditRepository.class);
    
    /**
     * Finds a credit record by donor ID.
     * 
     * @param donorId the donor's user ID
     * @return the credit record, or null if not found
     * @throws SQLException if database error occurs
     */
    @Override
    public Credit findByDonor(Long donorId) throws SQLException {
        String query = "SELECT * FROM credit_transactions WHERE donor_id = ? ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, donorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCredit(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Saves a new credit record.
     * 
     * @param credit the credit to save
     * @return the saved credit with generated ID
     * @throws SQLException if database error occurs
     */
    @Override
    public Credit save(Credit credit) throws SQLException {
        // Insert into credit_transactions table for transaction history
        String query = "INSERT INTO credit_transactions (donor_id, amount, type, source, created_at) VALUES (?, ?, 'EARNED', ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, credit.getDonorId());
            stmt.setInt(2, credit.getAmount());
            stmt.setString(3, credit.getSource());
            stmt.setTimestamp(4, credit.getEarnedDate() != null ? 
                    Timestamp.valueOf(credit.getEarnedDate()) : new Timestamp(System.currentTimeMillis()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating credit transaction failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    credit.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating credit transaction failed, no ID obtained.");
                }
            }
            
            // Update donor's credit_balance in users table
            updateDonorCreditBalance(credit.getDonorId(), credit.getAmount(), true);
            
            // Update or insert into credits table for balance tracking
            updateCreditsTableBalance(credit.getDonorId(), credit.getAmount());
            
            logger.info("Credit saved successfully: id={}, donorId={}, amount={}", 
                       credit.getId(), credit.getDonorId(), credit.getAmount());
            
            return credit;
        }
    }
    
    /**
     * Updates an existing credit record.
     * 
     * @param credit the credit to update
     * @throws SQLException if database error occurs
     */
    @Override
    public void update(Credit credit) throws SQLException {
        String query = "UPDATE credit_transactions SET donor_id = ?, amount = ?, type = 'EARNED', source = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, credit.getDonorId());
            stmt.setInt(2, credit.getAmount());
            stmt.setString(3, credit.getSource());
            stmt.setLong(4, credit.getId());
            
            stmt.executeUpdate();
            
            logger.info("Credit transaction updated successfully: id={}", credit.getId());
        }
    }
    
    /**
     * Adds credits to a donor's balance.
     * 
     * @param donorId the donor's user ID
     * @param amount the amount to add
     * @throws SQLException if database error occurs
     */
    @Override
    public void addCredits(Long donorId, double amount) throws SQLException {
        int credits = (int) Math.floor(amount);
        
        Credit credit = new Credit();
        credit.setDonorId(donorId);
        credit.setAmount(credits);
        credit.setSource("Donation");
        credit.setEarnedDate(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
        
        save(credit);
    }
    
    /**
     * Deducts credits from a donor's balance.
     * 
     * @param donorId the donor's user ID
     * @param amount the amount to deduct
     * @return true if successful, false if insufficient balance
     * @throws SQLException if database error occurs
     */
    @Override
    public boolean deductCredits(Long donorId, double amount) throws SQLException {
        int credits = (int) amount;
        double currentBalance = getBalance(donorId);
        
        if (currentBalance < credits) {
            return false;
        }
        
        return updateDonorCreditBalance(donorId, -credits, false);
    }
    
    /**
     * Gets the credit balance for a donor.
     * 
     * @param donorId the donor's user ID
     * @return the credit balance
     * @throws SQLException if database error occurs
     */
    @Override
    public double getBalance(Long donorId) throws SQLException {
        String query = "SELECT credit_balance FROM users WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, donorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("credit_balance");
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Updates the donor's credit_balance in the donors table.
     * 
     * @param donorId the donor's user ID
     * @param amount the amount to add or subtract
     * @param isAdd true to add, false to subtract
     * @return true if successful
     * @throws SQLException if database error occurs
     */
    private boolean updateDonorCreditBalance(Long donorId, int amount, boolean isAdd) throws SQLException {
        String query = isAdd ? 
                "UPDATE users SET credit_balance = credit_balance + ? WHERE id = ?" :
                "UPDATE users SET credit_balance = credit_balance - ? WHERE id = ? AND credit_balance >= ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            if (isAdd) {
                stmt.setInt(1, amount);
                stmt.setLong(2, donorId);
            } else {
                stmt.setInt(1, Math.abs(amount));
                stmt.setLong(2, donorId);
                stmt.setInt(3, Math.abs(amount));
            }
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Updates or creates a record in the credits table for balance tracking.
     * 
     * @param donorId the donor's user ID
     * @param amount the amount to add to balance
     * @throws SQLException if database error occurs
     */
    private void updateCreditsTableBalance(Long donorId, int amount) throws SQLException {
        String query = "INSERT INTO credits (donor_id, balance) VALUES (?, ?) " +
                      "ON DUPLICATE KEY UPDATE balance = balance + ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, donorId);
            stmt.setInt(2, amount);
            stmt.setInt(3, amount);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Maps ResultSet to Credit object.
     * 
     * @param rs the result set
     * @return the Credit object
     * @throws SQLException if database error occurs
     */
    private Credit mapResultSetToCredit(ResultSet rs) throws SQLException {
        Credit credit = new Credit();
        credit.setId(rs.getLong("id"));
        credit.setDonorId(rs.getLong("donor_id"));
        credit.setAmount(rs.getInt("amount"));
        
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            credit.setEarnedDate(createdTimestamp.toLocalDateTime());
        }
        
        credit.setSource(rs.getString("source"));
        
        return credit;
    }
}
