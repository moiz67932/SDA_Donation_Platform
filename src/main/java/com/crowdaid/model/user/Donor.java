package com.crowdaid.model.user;

import com.crowdaid.model.common.Role;

/**
 * Donor class representing users who make donations and subscriptions.
 * Donors can earn credits and redeem rewards.
 * 
 * Related to UC7 (Make Donation), UC8 (Subscribe), UC9 (Vote), and UC10 (Redeem Credits).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Donor extends User {
    
    private Credit credit;
    private Wallet wallet;
    private double creditBalance;
    
    /**
     * Default constructor initializing role as DONOR.
     */
    public Donor() {
        super();
        this.role = Role.DONOR;
    }
    
    /**
     * Constructor with basic user fields.
     * 
     * @param id the donor ID
     * @param name the donor's name
     * @param email the donor's email
     * @param passwordHash the hashed password
     * @param phone the donor's phone number
     * @param verified whether the account is verified
     */
    public Donor(Long id, String name, String email, String passwordHash, String phone, boolean verified) {
        super(id, name, email, passwordHash, phone, Role.DONOR, verified);
    }
    
    // Getters and Setters
    
    public Credit getCredit() {
        return credit;
    }
    
    public void setCredit(Credit credit) {
        this.credit = credit;
    }
    
    public Wallet getWallet() {
        return wallet;
    }
    
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
    
    /**
     * Gets the donor's current credit balance.
     * 
     * @return the credit balance
     */
    public double getCreditBalance() {
        return creditBalance;
    }
    
    /**
     * Sets the donor's credit balance.
     * 
     * @param creditBalance the new credit balance
     */
    public void setCreditBalance(double creditBalance) {
        this.creditBalance = creditBalance;
    }
    
    /**
     * Gets the donor's wallet balance.
     * 
     * @return the wallet balance, or 0 if wallet is null
     */
    public double getWalletBalance() {
        return wallet != null ? wallet.getBalance() : 0.0;
    }
    
    @Override
    public String toString() {
        return "Donor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", creditBalance=" + getCreditBalance() +
                ", verified=" + verified +
                '}';
    }
}
