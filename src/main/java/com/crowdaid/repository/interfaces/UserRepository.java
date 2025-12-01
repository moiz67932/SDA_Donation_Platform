package com.crowdaid.repository.interfaces;

import com.crowdaid.model.common.Role;
import com.crowdaid.model.user.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for User entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface UserRepository {
    
    /**
     * Finds a user by ID.
     * 
     * @param id the user ID
     * @return the user, or null if not found
     * @throws SQLException if database error occurs
     */
    User findById(Long id) throws SQLException;
    
    /**
     * Finds a user by email address.
     * 
     * @param email the email address
     * @return the user, or null if not found
     * @throws SQLException if database error occurs
     */
    User findByEmail(String email) throws SQLException;
    
    /**
     * Finds all users with a specific role.
     * 
     * @param role the user role
     * @return list of users with the role
     * @throws SQLException if database error occurs
     */
    List<User> findByRole(Role role) throws SQLException;
    
    /**
     * Saves a new user.
     * 
     * @param user the user to save
     * @return the saved user with generated ID
     * @throws SQLException if database error occurs
     */
    User save(User user) throws SQLException;
    
    /**
     * Updates an existing user.
     * 
     * @param user the user to update
     * @throws SQLException if database error occurs
     */
    void update(User user) throws SQLException;
    
    /**
     * Deletes a user by ID.
     * 
     * @param id the user ID
     * @throws SQLException if database error occurs
     */
    void delete(Long id) throws SQLException;
    
    /**
     * Checks if an email already exists.
     * 
     * @param email the email to check
     * @return true if email exists
     * @throws SQLException if database error occurs
     */
    boolean emailExists(String email) throws SQLException;
    
    /**
     * Finds all users.
     * 
     * @return list of all users
     * @throws SQLException if database error occurs
     */
    List<User> findAll() throws SQLException;
    
    /**
     * Counts all users.
     * 
     * @return total count of users
     * @throws SQLException if database error occurs
     */
    int countAll() throws SQLException;
}
