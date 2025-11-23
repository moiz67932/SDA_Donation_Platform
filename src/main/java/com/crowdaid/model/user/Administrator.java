package com.crowdaid.model.user;

import com.crowdaid.model.common.Role;

/**
 * Administrator class representing platform administrators.
 * Admins approve campaigns, manage rewards, and moderate the platform.
 * 
 * Related to UC11 (Approve Campaign) and UC12 (Edit Reward Shop).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Administrator extends User {
    
    /**
     * Default constructor initializing role as ADMIN.
     */
    public Administrator() {
        super();
        this.role = Role.ADMIN;
    }
    
    /**
     * Constructor with basic user fields.
     * 
     * @param id the admin ID
     * @param name the admin's name
     * @param email the admin's email
     * @param passwordHash the hashed password
     * @param phone the admin's phone number
     * @param verified whether the account is verified
     */
    public Administrator(Long id, String name, String email, String passwordHash, String phone, boolean verified) {
        super(id, name, email, passwordHash, phone, Role.ADMIN, verified);
    }
    
    @Override
    public String toString() {
        return "Administrator{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", verified=" + verified +
                '}';
    }
}
