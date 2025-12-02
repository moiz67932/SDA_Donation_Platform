package com.crowdaid.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * ViewLoader utility for loading and switching between FXML views.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class ViewLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(ViewLoader.class);
    private static ViewLoader instance;
    private Stage primaryStage;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private ViewLoader() {
    }
    
    /**
     * Gets the singleton instance.
     * 
     * @return the ViewLoader instance
     */
    public static synchronized ViewLoader getInstance() {
        if (instance == null) {
            instance = new ViewLoader();
        }
        return instance;
    }
    
    /**
     * Sets the primary stage for the application.
     * 
     * @param stage the primary stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    /**
     * Gets the primary stage for the application.
     * 
     * @return the primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Loads an FXML view and sets it as the scene for the given stage.
     * 
     * @param stage the stage to update
     * @param fxmlPath the path to the FXML file (relative to resources)
     * @param title the window title
     */
    public void loadView(Stage stage, String fxmlPath, String title) {
        try {
            // Remember the current window state
            boolean wasMaximized = stage.isMaximized();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            double currentX = stage.getX();
            double currentY = stage.getY();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            
            // Load CSS
            String css = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            stage.setScene(scene);
            stage.setTitle(title);
            
            // Restore the window state
            if (wasMaximized) {
                stage.setMaximized(true);
            } else {
                stage.setWidth(currentWidth);
                stage.setHeight(currentHeight);
                stage.setX(currentX);
                stage.setY(currentY);
            }
            
            logger.info("Loaded view: {}", fxmlPath);
            
        } catch (IOException e) {
            logger.error("Failed to load view: {}", fxmlPath, e);
            AlertUtil.showError("View Loading Error", "Failed to load view: " + fxmlPath);
        }
    }
    
    /**
     * Loads an FXML view and returns the FXMLLoader for controller access.
     * 
     * @param fxmlPath the path to the FXML file
     * @return the FXMLLoader instance
     * @throws IOException if loading fails
     */
    public FXMLLoader loadViewWithLoader(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.load();
        return loader;
    }
    
    /**
     * Loads a view and returns the root Parent node.
     * 
     * @param fxmlPath the path to the FXML file
     * @return the root Parent node
     * @throws IOException if loading fails
     */
    public Parent loadViewAsParent(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        return loader.load();
    }
    
    /**
     * Loads an FXML view as a dialog in a new stage.
     * 
     * @param fxmlPath the path to the FXML file
     * @param title the dialog title
     */
    public void loadDialog(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            Scene scene = new Scene(root);
            
            // Load CSS
            String css = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            dialogStage.setScene(scene);
            dialogStage.setTitle(title);
            dialogStage.showAndWait();
            
            logger.info("Loaded dialog: {}", fxmlPath);
            
        } catch (IOException e) {
            logger.error("Failed to load dialog: {}", fxmlPath, e);
            AlertUtil.showError("Dialog Loading Error", "Failed to load dialog: " + fxmlPath);
        }
    }
}
