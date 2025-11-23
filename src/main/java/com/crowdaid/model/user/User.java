package com.crowdaid.model.user;

import com.crowdaid.model.common.BaseEntity;
import com.crowdaid.model.common.Role;

/**
 * Abstract User class representing all users in the CrowdAid platform.
 * Serves as base class for Donor, Campaigner, and Administrator.
 * 
 * Related to UC1 (Register Account) and UC2 (Login).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public abstract class User extends BaseEntity {
    
    protected String name;
    protected String email;
    protected String passwordHash;
    protected String phone;
    protected Role role;
    protected boolean verified;
    
    /**
     * Default constructor.
     */
    public User() {
        super();
        this.verified = false;
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param id the user ID
     * @param name the user's full name
     * @param email the user's email address
     * @param passwordHash the hashed password
     * @param phone the user's phone number
     * @param role the user's role
     * @param verified whether the account is verified
     */
    public User(Long id, String name, String email, String passwordHash, String phone, Role role, boolean verified) {
        super(id);
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
        this.verified = verified;
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", verified=" + verified +
                '}';
    }
}
