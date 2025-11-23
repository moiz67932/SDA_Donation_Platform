package com.crowdaid.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Singleton for MySQL connectivity.
 * Manages database connections to the fundraiser_db database.
 * 
 * Configuration:
 * - Host: 192.168.100.10
 * - Port: 3306
 * - Database: fundraiser_db
 * - User: dbuser (should be updated with actual credentials)
 * - Password: dbpassword (should be updated with actual credentials)
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class DBConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    
    // Database configuration
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "fundraising_platform";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "moiz123";
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME 
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    
    // Singleton instance
    private static DBConnection instance;
    
    /**
     * Private constructor to prevent instantiation.
     * Loads the MySQL JDBC driver.
     */
    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.error("MySQL JDBC Driver not found", e);
            throw new RuntimeException("Failed to load MySQL JDBC Driver", e);
        }
    }
    
    /**
     * Gets the singleton instance of DBConnection.
     * 
     * @return the DBConnection instance
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }
    
    /**
     * Establishes and returns a connection to the MySQL database.
     * 
     * @return a Connection object to the database
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            logger.debug("Database connection established");
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to establish database connection", e);
            throw e;
        }
    }
    
    /**
     * Tests the database connection.
     * 
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            logger.info("Database connection test successful");
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * Closes the given database connection.
     * 
     * @param connection the connection to close
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.debug("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }
}
