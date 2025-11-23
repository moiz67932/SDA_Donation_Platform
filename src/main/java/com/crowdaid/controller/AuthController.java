package com.crowdaid.controller;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.common.Role;
import com.crowdaid.model.user.User;
import com.crowdaid.service.AuthenticationService;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for login and registration screens.
 * Handles UC1 (Register Account) and UC2 (Login).
 */
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthenticationService authService;
    private final ViewLoader viewLoader;
    
    // Login fields
    @FXML private TextField loginEmailField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    
    // Registration fields
    @FXML private TextField regNameField;
    @FXML private TextField regEmailField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private TextField regPhoneField;
    @FXML private RadioButton donorRadio;
    @FXML private RadioButton campaignerRadio;
    @FXML private ToggleGroup roleGroup;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    
    public AuthController() {
        this.authService = new AuthenticationService();
        this.viewLoader = ViewLoader.getInstance();
    }
    
    @FXML
    private void initialize() {
        logger.info("AuthController initialized");
    }
    
    /**
     * Handles login button click (UC2: Login).
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = loginEmailField.getText();
        String password = loginPasswordField.getText();
        
        try {
            User user = authService.login(email, password);
            SessionManager.getInstance().setCurrentUser(user);
            
            logger.info("User logged in: {}, role: {}", user.getEmail(), user.getRole());
            
            // Navigate to appropriate dashboard
            navigateToDashboard(user.getRole());
            
        } catch (ValidationException e) {
            AlertUtil.showError("Validation Error", e.getMessage());
        } catch (BusinessException e) {
            AlertUtil.showError("Login Failed", e.getMessage());
        }
    }
    
    /**
     * Handles registration button click (UC1: Register Account).
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String name = regNameField.getText();
        String email = regEmailField.getText();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();
        String phone = regPhoneField.getText();
        
        RadioButton selectedRole = (RadioButton) roleGroup.getSelectedToggle();
        if (selectedRole == null) {
            AlertUtil.showError("Validation Error", "Please select a role (Donor or Campaigner)");
            return;
        }
        
        try {
            if (selectedRole == donorRadio) {
                authService.registerDonor(name, email, password, confirmPassword, phone);
            } else {
                authService.registerCampaigner(name, email, password, confirmPassword, phone);
            }
            
            AlertUtil.showSuccess("Registration Successful", 
                "Your account has been created successfully. You can now log in.");
            
            // Navigate to login
            viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
            
        } catch (ValidationException e) {
            AlertUtil.showError("Validation Error", e.getMessage());
        } catch (BusinessException e) {
            AlertUtil.showError("Registration Failed", e.getMessage());
        }
    }
    
    /**
     * Navigates to register screen.
     */
    @FXML
    private void handleRegisterLink(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/register.fxml", "CrowdAid - Register");
    }
    
    /**
     * Navigates to login screen.
     */
    @FXML
    private void handleLoginLink(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
    }
    
    /**
     * Navigates to appropriate dashboard based on user role.
     */
    private void navigateToDashboard(Role role) {
        switch (role) {
            case DONOR:
                viewLoader.loadView(viewLoader.getPrimaryStage(), 
                    "/fxml/donor_dashboard.fxml", "CrowdAid - Donor Dashboard");
                break;
            case CAMPAIGNER:
                viewLoader.loadView(viewLoader.getPrimaryStage(), 
                    "/fxml/campaigner_dashboard.fxml", "CrowdAid - Campaigner Dashboard");
                break;
            case ADMIN:
                viewLoader.loadView(viewLoader.getPrimaryStage(), 
                    "/fxml/admin_dashboard.fxml", "CrowdAid - Admin Dashboard");
                break;
        }
    }
}
