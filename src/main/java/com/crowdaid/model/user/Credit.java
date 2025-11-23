package com.crowdaid.model.user;

import com.crowdaid.model.common.BaseEntity;

/**
 * Credit class representing virtual credits earned by donors.
 * Credits can be redeemed in the reward shop.
 * 
 * Related to UC10 (Redeem Credits in Shop).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Credit extends BaseEntity {
    
    private Long donorId;
    private double balance;
    
    /**
     * Default constructor initializing balance to zero.
     */
    public Credit() {
        super();
        this.balance = 0.0;
    }
    
    /**
     * Constructor with donor ID and initial balance.
     * 
     * @param donorId the donor's user ID
     * @param balance the initial credit balance
     */
    public Credit(Long donorId, double balance) {
        super();
        this.donorId = donorId;
        this.balance = balance;
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param id the credit record ID
     * @param donorId the donor's user ID
     * @param balance the credit balance
     */
    public Credit(Long id, Long donorId, double balance) {
        super(id);
        this.donorId = donorId;
        this.balance = balance;
    }
    
    // Getters and Setters
    
    public Long getDonorId() {
        return donorId;
    }
    
    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    /**
     * Adds credits to the balance.
     * 
     * @param amount the amount to add
     */
    public void addCredits(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.touch();
        }
    }
    
    /**
     * Deducts credits from the balance.
     * 
     * @param amount the amount to deduct
     * @return true if successful, false if insufficient balance
     */
    public boolean deductCredits(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            this.touch();
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "Credit{" +
                "id=" + id +
                ", donorId=" + donorId +
                ", balance=" + balance +
                '}';
    }
}
