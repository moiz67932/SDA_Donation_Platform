package com.crowdaid.service;

import com.crowdaid.config.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Bootstrap Service for initializing database with seed data.
 * Ensures required data exists on application startup.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class BootstrapService {
    
    private static final Logger logger = LoggerFactory.getLogger(BootstrapService.class);
    
    /**
     * Initializes the database with seed data if not already present.
     */
    public void initialize() {
        logger.info("Starting database bootstrap...");
        
        try {
            ensureAdminExists();
            ensurePaymentGatewayExists();
            ensureSampleRewardsExist();
            
            logger.info("Database bootstrap completed successfully");
        } catch (Exception e) {
            logger.error("Error during database bootstrap", e);
            // Don't throw - allow app to start even if bootstrap fails
        }
    }
    
    /**
     * Ensures at least one admin user exists in the system.
     */
    private void ensureAdminExists() throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
            
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int adminCount = rs.getInt(1);
            
            if (adminCount == 0) {
                createAdminUser();
                logger.info("✓ Admin user created");
            } else {
                logger.info("✓ Admin user already exists");
            }
        }
    }
    
    /**
     * Creates the default admin user.
     */
    private void createAdminUser() throws SQLException {
        String insertUser = "INSERT INTO users (name, email, password_hash, role, is_verified) " +
                           "VALUES (?, ?, ?, 'ADMIN', 1)";
        String insertAdmin = "INSERT INTO admins (user_id) VALUES (?)";
        
        // Hash the password using BCrypt
        String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
        
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement userStmt = conn.prepareStatement(insertUser, 
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                
                userStmt.setString(1, "System Administrator");
                userStmt.setString(2, "admin@crowdaid.com");
                userStmt.setString(3, hashedPassword);
                userStmt.executeUpdate();
                
                ResultSet keys = userStmt.getGeneratedKeys();
                if (keys.next()) {
                    long userId = keys.getLong(1);
                    
                    try (PreparedStatement adminStmt = conn.prepareStatement(insertAdmin)) {
                        adminStmt.setLong(1, userId);
                        adminStmt.executeUpdate();
                    }
                }
                
                conn.commit();
                logger.info("Created admin user: admin@crowdaid.com / admin123");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
    /**
     * Ensures payment gateway exists.
     */
    private void ensurePaymentGatewayExists() throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM payment_gateways";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
            
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int gatewayCount = rs.getInt(1);
            
            if (gatewayCount == 0) {
                createPaymentGateway();
                logger.info("✓ Payment gateway created");
            } else {
                logger.info("✓ Payment gateway already exists");
            }
        }
    }
    
    /**
     * Creates simulated payment gateway.
     */
    private void createPaymentGateway() throws SQLException {
        String insertQuery = "INSERT INTO payment_gateways (name) VALUES ('Simulated Payment Gateway')";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Ensures sample rewards exist in the shop.
     */
    private void ensureSampleRewardsExist() throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM rewards";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
            
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int rewardCount = rs.getInt(1);
            
            if (rewardCount == 0) {
                createSampleRewards();
                logger.info("✓ Sample rewards created");
            } else {
                logger.info("✓ Rewards already exist");
            }
        }
    }
    
    /**
     * Creates sample rewards for testing.
     */
    private void createSampleRewards() throws SQLException {
        String insertQuery = "INSERT INTO rewards (name, description, credit_cost, category, " +
                            "stock_quantity, is_available) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            
            // Reward 1: Digital Badge
            stmt.setString(1, "Donor Badge - Bronze");
            stmt.setString(2, "Digital badge recognizing your generous donation");
            stmt.setInt(3, 10);
            stmt.setString(4, "DIGITAL");
            stmt.setInt(5, 1000);
            stmt.setBoolean(6, true);
            stmt.executeUpdate();
            
            // Reward 2: T-Shirt
            stmt.setString(1, "CrowdAid T-Shirt");
            stmt.setString(2, "Official CrowdAid branded t-shirt (shipped to your address)");
            stmt.setInt(3, 50);
            stmt.setString(4, "PHYSICAL");
            stmt.setInt(5, 100);
            stmt.setBoolean(6, true);
            stmt.executeUpdate();
            
            // Reward 3: Certificate
            stmt.setString(1, "Appreciation Certificate");
            stmt.setString(2, "Personalized certificate of appreciation");
            stmt.setInt(3, 25);
            stmt.setString(4, "DIGITAL");
            stmt.setInt(5, 500);
            stmt.setBoolean(6, true);
            stmt.executeUpdate();
            
            // Reward 4: Premium Badge
            stmt.setString(1, "Donor Badge - Gold");
            stmt.setString(2, "Exclusive gold-tier digital badge for top donors");
            stmt.setInt(3, 100);
            stmt.setString(4, "DIGITAL");
            stmt.setInt(5, 200);
            stmt.setBoolean(6, true);
            stmt.executeUpdate();
            
            // Reward 5: Gift Card
            stmt.setString(1, "$10 Gift Card");
            stmt.setString(2, "Digital gift card redeemable at partner stores");
            stmt.setInt(3, 75);
            stmt.setString(4, "DIGITAL");
            stmt.setInt(5, 50);
            stmt.setBoolean(6, true);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Tests database connection.
     * 
     * @return true if connection successful
     */
    public boolean testConnection() {
        return DBConnection.getInstance().testConnection();
    }
}
