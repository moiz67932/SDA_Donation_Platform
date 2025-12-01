package com.crowdaid;

import com.crowdaid.service.BootstrapService;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Application Entry Point for CrowdAid Fundraising Platform.
 * This JavaFX application implements an online fundraising system with
 * one-time donations, subscriptions, escrow management, milestone voting,
 * and a reward shop system.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MainApp extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private static Stage primaryStage;
    
    /**
     * JavaFX application start method.
     * Initializes the primary stage and loads the login screen.
     * 
     * @param stage the primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        try {
            // Initialize database with seed data
            logger.info("Initializing database...");
            BootstrapService bootstrapService = new BootstrapService();
            
            // Test database connection
            if (!bootstrapService.testConnection()) {
                logger.error("Database connection failed!");
                showErrorAndExit("Database connection failed. Please check your database configuration.");
                return;
            }
            
            // Run bootstrap to create admin and sample data
            bootstrapService.initialize();
            
            primaryStage = stage;
            primaryStage.setTitle("CrowdAid - Online Fundraising Platform");
            
            // Set the primary stage in ViewLoader
            ViewLoader.getInstance().setPrimaryStage(primaryStage);
            
            // Load the login screen
            ViewLoader.getInstance().loadView(primaryStage, "/fxml/login.fxml", "CrowdAid - Login");
            
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(768);
            primaryStage.show();
            
            logger.info("CrowdAid application started successfully");
            logger.info("Default admin credentials: admin@crowdaid.com / admin123");
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            e.printStackTrace();
            showErrorAndExit("Failed to start application: " + e.getMessage());
        }
    }
    
    /**
     * Shows error dialog and exits application.
     * 
     * @param message the error message
     */
    private void showErrorAndExit(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Startup Error");
        alert.setHeaderText("Failed to start CrowdAid");
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }
    
    /**
     * Application stop method.
     * Cleans up resources when the application is closed.
     */
    @Override
    public void stop() {
        SessionManager.getInstance().clear();
        logger.info("CrowdAid application stopped");
    }
    
    /**
     * Gets the primary stage of the application.
     * 
     * @return the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Main method to launch the JavaFX application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        logger.info("Launching CrowdAid Fundraising Platform...");
        launch(args);
    }
}
