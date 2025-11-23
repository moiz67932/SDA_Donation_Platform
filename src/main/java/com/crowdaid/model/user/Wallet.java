package com.crowdaid.model.user;

import com.crowdaid.model.common.BaseEntity;

/**
 * Wallet class representing a user's monetary wallet balance.
 * Used for storing funds and processing transactions.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Wallet extends BaseEntity {
    
    private Long userId;
    private double balance;
    
    /**
     * Default constructor initializing balance to zero.
     */
    public Wallet() {
        super();
        this.balance = 0.0;
    }
    
    /**
     * Constructor with user ID and initial balance.
     * 
     * @param userId the user's ID
     * @param balance the initial wallet balance
     */
    public Wallet(Long userId, double balance) {
        super();
        this.userId = userId;
        this.balance = balance;
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param id the wallet ID
     * @param userId the user's ID
     * @param balance the wallet balance
     */
    public Wallet(Long id, Long userId, double balance) {
        super(id);
        this.userId = userId;
        this.balance = balance;
    }
    
    // Getters and Setters
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    /**
     * Adds funds to the wallet.
     * 
     * @param amount the amount to add
     */
    public void addFunds(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.touch();
        }
    }
    
    /**
     * Deducts funds from the wallet.
     * 
     * @param amount the amount to deduct
     * @return true if successful, false if insufficient balance
     */
    public boolean deductFunds(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            this.touch();
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", userId=" + userId +
                ", balance=" + balance +
                '}';
    }
}
