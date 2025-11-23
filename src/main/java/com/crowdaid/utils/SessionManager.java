package com.crowdaid.utils;

import com.crowdaid.model.user.User;
import java.util.HashMap;
import java.util.Map;

/**
 * SessionManager singleton for managing the current logged-in user session.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    private Map<String, Object> attributes;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private SessionManager() {
        this.attributes = new HashMap<>();
    }
    
    /**
     * Gets the singleton instance.
     * 
     * @return the SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Sets the current logged-in user.
     * 
     * @param user the user to set
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Gets the current logged-in user.
     * 
     * @return the current user, or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if a user is currently logged in.
     * 
     * @return true if a user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Clears the current session (logout).
     */
    public void clear() {
        this.currentUser = null;
        this.attributes.clear();
    }
    
    /**
     * Sets a session attribute.
     * 
     * @param key the attribute key
     * @param value the attribute value
     */
    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }
    
    /**
     * Gets a session attribute.
     * 
     * @param key the attribute key
     * @return the attribute value, or null if not found
     */
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }
    
    /**
     * Removes a session attribute.
     * 
     * @param key the attribute key
     */
    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }
}
