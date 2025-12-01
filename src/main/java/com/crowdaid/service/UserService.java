package com.crowdaid.service;

import com.crowdaid.exception.BusinessException;
import com.crowdaid.exception.ValidationException;
import com.crowdaid.model.user.User;
import com.crowdaid.repository.interfaces.UserRepository;
import com.crowdaid.repository.mysql.MySQLUserRepository;
import com.crowdaid.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * UserService handles user management operations.
 * 
 * Provides CRUD operations for users and implements GRASP principles:
 * - Information Expert: User-related operations
 * - Low Coupling: Depends only on UserRepository interface
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    
    /**
     * Constructor initializing the user repository.
     */
    public UserService() {
        this.userRepository = new MySQLUserRepository();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param userRepository the user repository
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Finds a user by ID.
     * 
     * @param userId the user ID
     * @return the user
     * @throws ValidationException if validation fails
     * @throws BusinessException if user not found
     */
    public User getUserById(Long userId) throws ValidationException, BusinessException {
        Validator.validatePositive(userId, "User ID");
        
        try {
            User user = userRepository.findById(userId);
            
            if (user == null) {
                logger.warn("User not found: id={}", userId);
                throw new BusinessException("User not found with ID: " + userId);
            }
            
            logger.debug("User retrieved: id={}, email={}", user.getId(), user.getEmail());
            return user;
            
        } catch (SQLException e) {
            logger.error("Database error while finding user by ID", e);
            throw new BusinessException("Failed to retrieve user", e);
        }
    }
    
    /**
     * Finds a user by email.
     * 
     * @param email the user's email
     * @return the user
     * @throws ValidationException if validation fails
     * @throws BusinessException if user not found
     */
    public User getUserByEmail(String email) throws ValidationException, BusinessException {
        Validator.validateEmail(email);
        
        try {
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                logger.warn("User not found: email={}", email);
                throw new BusinessException("User not found with email: " + email);
            }
            
            logger.debug("User retrieved: id={}, email={}", user.getId(), user.getEmail());
            return user;
            
        } catch (SQLException e) {
            logger.error("Database error while finding user by email", e);
            throw new BusinessException("Failed to retrieve user", e);
        }
    }
    
    /**
     * Updates user profile information.
     * 
     * @param user the user with updated information
     * @throws ValidationException if validation fails
     * @throws BusinessException if update fails
     */
    public void updateUser(User user) throws ValidationException, BusinessException {
        Validator.validateNotNull(user, "User");
        Validator.validatePositive(user.getId(), "User ID");
        Validator.validateNonEmpty(user.getFullName(), "Full name");
        Validator.validateEmail(user.getEmail());
        
        try {
            userRepository.update(user);
            logger.info("User updated successfully: id={}, email={}", user.getId(), user.getEmail());
            
        } catch (SQLException e) {
            logger.error("Database error while updating user", e);
            throw new BusinessException("Failed to update user", e);
        }
    }
    
    /**
     * Verifies a user's email.
     * 
     * @param userId the user ID
     * @throws ValidationException if validation fails
     * @throws BusinessException if verification fails
     */
    public void verifyUser(Long userId) throws ValidationException, BusinessException {
        Validator.validatePositive(userId, "User ID");
        
        try {
            User user = userRepository.findById(userId);
            
            if (user == null) {
                throw new BusinessException("User not found");
            }
            
            if (user.isVerified()) {
                logger.warn("User already verified: id={}", userId);
                return;
            }
            
            user.setVerified(true);
            userRepository.update(user);
            
            logger.info("User verified successfully: id={}, email={}", userId, user.getEmail());
            
        } catch (SQLException e) {
            logger.error("Database error while verifying user", e);
            throw new BusinessException("Failed to verify user", e);
        }
    }
    
    /**
     * Retrieves all users (admin operation).
     * 
     * @return list of all users
     * @throws BusinessException if retrieval fails
     */
    public List<User> getAllUsers() throws BusinessException {
        try {
            List<User> users = userRepository.findAll();
            logger.debug("Retrieved {} users", users.size());
            return users;
            
        } catch (SQLException e) {
            logger.error("Database error while retrieving all users", e);
            throw new BusinessException("Failed to retrieve users", e);
        }
    }
    
    /**
     * Checks if an email is already registered.
     * 
     * @param email the email to check
     * @return true if email exists
     * @throws ValidationException if validation fails
     */
    public boolean emailExists(String email) throws ValidationException {
        Validator.validateEmail(email);
        
        try {
            return userRepository.findByEmail(email) != null;
            
        } catch (SQLException e) {
            logger.error("Database error while checking email existence", e);
            return false;
        }
    }
}
