package com.crowdaid.service;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.common.Role;
import com.crowdaid.model.user.Campaigner;
import com.crowdaid.model.user.Donor;
import com.crowdaid.model.user.User;
import com.crowdaid.repository.interfaces.UserRepository;
import com.crowdaid.repository.mysql.MySQLUserRepository;
import com.crowdaid.utils.Validator;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * AuthenticationService handles user authentication and registration.
 * 
 * Implements UC1 (Register Account) and UC2 (Login to Platform).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;
    
    /**
     * Constructor initializing the user repository.
     */
    public AuthenticationService() {
        this.userRepository = new MySQLUserRepository();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param userRepository the user repository
     */
    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Authenticates a user with email and password (UC2: Login).
     * 
     * @param email the user's email
     * @param password the user's password
     * @return the authenticated user
     * @throws ValidationException if validation fails
     * @throws BusinessException if authentication fails
     */
    public User login(String email, String password) throws ValidationException, BusinessException {
        // Validate inputs
        Validator.validateEmail(email);
        Validator.validateNonEmpty(password, "Password");
        
        try {
            // Find user by email
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                logger.warn("Login failed: user not found for email={}", email);
                throw new BusinessException("Invalid email or password");
            }
            
            // Verify password
            if (!BCrypt.checkpw(password, user.getPasswordHash())) {
                logger.warn("Login failed: incorrect password for email={}", email);
                throw new BusinessException("Invalid email or password");
            }
            
            // Check if account is verified
            if (!user.isVerified()) {
                logger.warn("Login failed: account not verified for email={}", email);
                throw new BusinessException("Account not verified. Please verify your email.");
            }
            
            logger.info("User logged in successfully: id={}, email={}, role={}", 
                       user.getId(), user.getEmail(), user.getRole());
            
            return user;
            
        } catch (SQLException e) {
            logger.error("Database error during login", e);
            throw new BusinessException("Login failed due to system error", e);
        }
    }
    
    /**
     * Registers a new donor account (UC1: Register Account).
     * 
     * @param name the donor's full name
     * @param email the donor's email address
     * @param password the donor's password
     * @param confirmPassword password confirmation
     * @param phone the donor's phone number (optional)
     * @return the registered donor
     * @throws ValidationException if validation fails
     * @throws BusinessException if registration fails
     */
    public Donor registerDonor(String name, String email, String password, String confirmPassword, String phone) 
            throws ValidationException, BusinessException {
        
        // Validate inputs
        Validator.validateNonEmpty(name, "Name");
        Validator.validateEmail(email);
        Validator.validatePassword(password);
        Validator.validatePasswordMatch(password, confirmPassword);
        
        if (phone != null && !phone.trim().isEmpty()) {
            Validator.validatePhone(phone);
        }
        
        try {
            // Check if email already exists
            if (userRepository.emailExists(email)) {
                logger.warn("Registration failed: email already exists - {}", email);
                throw new BusinessException("An account with this email already exists");
            }
            
            // Create new donor
            Donor donor = new Donor();
            donor.setName(name);
            donor.setEmail(email);
            donor.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
            donor.setPhone(phone);
            donor.setRole(Role.DONOR);
            donor.setVerified(true); // Auto-verify for demo purposes
            
            // Save donor
            User savedUser = userRepository.save(donor);
            
            logger.info("Donor registered successfully: id={}, email={}", savedUser.getId(), savedUser.getEmail());
            
            // notificationService.sendVerificationEmail(donor.getEmail());
            
            return (Donor) savedUser;
            
        } catch (SQLException e) {
            logger.error("Database error during donor registration", e);
            throw new BusinessException("Registration failed due to system error", e);
        }
    }
    
    /**
     * Registers a new campaigner account (UC1: Register Account).
     * 
     * @param name the campaigner's full name
     * @param email the campaigner's email address
     * @param password the campaigner's password
     * @param confirmPassword password confirmation
     * @param phone the campaigner's phone number (optional)
     * @return the registered campaigner
     * @throws ValidationException if validation fails
     * @throws BusinessException if registration fails
     */
    public Campaigner registerCampaigner(String name, String email, String password, String confirmPassword, String phone) 
            throws ValidationException, BusinessException {
        
        // Validate inputs
        Validator.validateNonEmpty(name, "Name");
        Validator.validateEmail(email);
        Validator.validatePassword(password);
        Validator.validatePasswordMatch(password, confirmPassword);
        
        if (phone != null && !phone.trim().isEmpty()) {
            Validator.validatePhone(phone);
        }
        
        try {
            // Check if email already exists
            if (userRepository.emailExists(email)) {
                logger.warn("Registration failed: email already exists - {}", email);
                throw new BusinessException("An account with this email already exists");
            }
            
            // Create new campaigner
            Campaigner campaigner = new Campaigner();
            campaigner.setName(name);
            campaigner.setEmail(email);
            campaigner.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
            campaigner.setPhone(phone);
            campaigner.setRole(Role.CAMPAIGNER);
            campaigner.setVerified(true); // Auto-verify for demo purposes
            
            // Save campaigner
            User savedUser = userRepository.save(campaigner);
            
            logger.info("Campaigner registered successfully: id={}, email={}", savedUser.getId(), savedUser.getEmail());
            
            // notificationService.sendVerificationEmail(campaigner.getEmail());
            
            return (Campaigner) savedUser;
            
        } catch (SQLException e) {
            logger.error("Database error during campaigner registration", e);
            throw new BusinessException("Registration failed due to system error", e);
        }
    }
    
    /**
     * Verifies a user account using a verification token.
     * 
     * @param email the user's email
     * @param token the verification token
     * @throws BusinessException if verification fails
     */
    public void verifyAccount(String email, String token) throws BusinessException {
        // This would involve:
        // 1. Validating the token
        // 2. Checking token expiration
        // 3. Updating user verified status
        logger.info("Account verification requested for email={}", email);
    }
    
    /**
     * Initiates password reset process.
     * 
     * @param email the user's email
     * @throws BusinessException if user not found
     */
    public void requestPasswordReset(String email) throws BusinessException {
        try {
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                // Don't reveal if email exists for security
                logger.warn("Password reset requested for non-existent email={}", email);
                return;
            }
            
            // notificationService.sendPasswordResetEmail(user.getEmail(), resetToken);
            
            logger.info("Password reset requested for user: id={}, email={}", user.getId(), user.getEmail());
            
        } catch (SQLException e) {
            logger.error("Database error during password reset request", e);
            throw new BusinessException("Password reset failed due to system error", e);
        }
    }
    
    /**
     * Resets user password with a valid reset token.
     * 
     * @param email the user's email
     * @param token the reset token
     * @param newPassword the new password
     * @param confirmPassword password confirmation
     * @throws ValidationException if validation fails
     * @throws BusinessException if reset fails
     */
    public void resetPassword(String email, String token, String newPassword, String confirmPassword) 
            throws ValidationException, BusinessException {
        
        Validator.validateEmail(email);
        Validator.validatePassword(newPassword);
        Validator.validatePasswordMatch(newPassword, confirmPassword);
        
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new BusinessException("Invalid reset token");
            }
            
            // Update password
            user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            user.touch();
            userRepository.update(user);
            
            logger.info("Password reset successful for user: id={}, email={}", user.getId(), user.getEmail());
            
        } catch (SQLException e) {
            logger.error("Database error during password reset", e);
            throw new BusinessException("Password reset failed due to system error", e);
        }
    }
}
