package com.crowdaid.model.user;

import com.crowdaid.model.common.Role;

/**
 * Campaigner class representing users who create and manage fundraising campaigns.
 * 
 * Related to UC3 (Create Campaign), UC4 (Set Milestones), and UC5 (Submit Milestone).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Campaigner extends User {
    
    private BankInfo bankInfo;
    private Wallet wallet;
    private double totalWithdrawn;
    
    /**
     * Default constructor initializing role as CAMPAIGNER.
     */
    public Campaigner() {
        super();
        this.role = Role.CAMPAIGNER;
    }
    
    /**
     * Constructor with basic user fields.
     * 
     * @param id the campaigner ID
     * @param name the campaigner's name
     * @param email the campaigner's email
     * @param passwordHash the hashed password
     * @param phone the campaigner's phone number
     * @param verified whether the account is verified
     */
    public Campaigner(Long id, String name, String email, String passwordHash, String phone, boolean verified) {
        super(id, name, email, passwordHash, phone, Role.CAMPAIGNER, verified);
    }
    
    // Getters and Setters
    
    public BankInfo getBankInfo() {
        return bankInfo;
    }
    
    public void setBankInfo(BankInfo bankInfo) {
        this.bankInfo = bankInfo;
    }
    
    public Wallet getWallet() {
        return wallet;
    }
    
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
    
    /**
     * Gets the campaigner's wallet balance.
     * 
     * @return the wallet balance, or 0 if wallet is null
     */
    public double getWalletBalance() {
        return wallet != null ? wallet.getBalance() : 0.0;
    }
    
    /**
     * Gets the total amount withdrawn from escrow across all campaigns.
     * 
     * @return the total withdrawn amount
     */
    public double getTotalWithdrawn() {
        return totalWithdrawn;
    }
    
    /**
     * Sets the total amount withdrawn from escrow.
     * 
     * @param totalWithdrawn the total withdrawn amount
     */
    public void setTotalWithdrawn(double totalWithdrawn) {
        this.totalWithdrawn = totalWithdrawn;
    }
    
    @Override
    public String toString() {
        return "Campaigner{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", verified=" + verified +
                ", hasBankInfo=" + (bankInfo != null) +
                '}';
    }
}
