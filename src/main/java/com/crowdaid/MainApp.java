package com.crowdaid;

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
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            e.printStackTrace();
        }
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
