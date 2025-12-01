package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.donation.Donation;
import com.crowdaid.repository.interfaces.DonationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of DonationRepository.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLDonationRepository implements DonationRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLDonationRepository.class);
    
    @Override
    public Donation findById(Long id) throws SQLException {
        String sql = "SELECT * FROM donations WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToDonation(rs);
            }
            return null;
        }
    }
    
    @Override
    public List<Donation> findByCampaign(Long campaignId) throws SQLException {
        String sql = "SELECT * FROM donations WHERE campaign_id = ? ORDER BY created_at DESC";
        List<Donation> donations = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaignId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                donations.add(mapResultSetToDonation(rs));
            }
            return donations;
        }
    }
    
    @Override
    public List<Donation> findByDonor(Long donorId) throws SQLException {
        String sql = "SELECT * FROM donations WHERE donor_id = ? ORDER BY created_at DESC";
        List<Donation> donations = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, donorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                donations.add(mapResultSetToDonation(rs));
            }
            return donations;
        }
    }
    
    @Override
    public double getTotalDonationByDonorToCampaign(Long donorId, Long campaignId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM donations " +
                     "WHERE donor_id = ? AND campaign_id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, donorId);
            stmt.setLong(2, campaignId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
            return 0.0;
        }
    }
    
    @Override
    public Donation save(Donation donation) throws SQLException {
        String sql = "INSERT INTO donations (campaign_id, donor_id, amount, is_anonymous, message, " +
                     "transaction_reference, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, donation.getCampaignId());
            stmt.setLong(2, donation.getDonorId());
            stmt.setDouble(3, donation.getAmount());
            stmt.setBoolean(4, donation.isAnonymous());
            stmt.setString(5, donation.getMessage());
            stmt.setString(6, donation.getTransactionReference());
            stmt.setTimestamp(7, Timestamp.valueOf(donation.getCreatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating donation failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    donation.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating donation failed, no ID obtained.");
                }
            }
            
            logger.info("Donation created: id={}, amount={}, campaignId={}", 
                       donation.getId(), donation.getAmount(), donation.getCampaignId());
            return donation;
        }
    }
    
    @Override
    public List<Donation> getTopDonors(Long campaignId, int limit) throws SQLException {
        String sql = "SELECT donor_id, SUM(amount) as total_amount, MAX(created_at) as last_donation " +
                     "FROM donations WHERE campaign_id = ? AND is_anonymous = FALSE " +
                     "GROUP BY donor_id ORDER BY total_amount DESC LIMIT ?";
        List<Donation> topDonors = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaignId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Donation donation = new Donation();
                donation.setDonorId(rs.getLong("donor_id"));
                donation.setAmount(rs.getDouble("total_amount"));
                donation.setCreatedAt(rs.getTimestamp("last_donation").toLocalDateTime());
                topDonors.add(donation);
            }
            return topDonors;
        }
    }
    
    /**
     * Maps a ResultSet row to a Donation object.
     * 
     * @param rs the ResultSet
     * @return the Donation object
     * @throws SQLException if database error occurs
     */
    private Donation mapResultSetToDonation(ResultSet rs) throws SQLException {
        Donation donation = new Donation();
        
        donation.setId(rs.getLong("id"));
        donation.setCampaignId(rs.getLong("campaign_id"));
        donation.setDonorId(rs.getLong("donor_id"));
        donation.setAmount(rs.getDouble("amount"));
        donation.setAnonymous(rs.getBoolean("is_anonymous"));
        donation.setMessage(rs.getString("message"));
        donation.setTransactionReference(rs.getString("transaction_reference"));
        donation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        return donation;
    }
    
    @Override
    public double getTotalDonationAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM donations";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
            return 0.0;
        }
    }
    
    @Override
    public int getUniqueDonorCount(Long campaignId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT donor_id) as count FROM donations WHERE campaign_id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, campaignId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        }
    }
}
