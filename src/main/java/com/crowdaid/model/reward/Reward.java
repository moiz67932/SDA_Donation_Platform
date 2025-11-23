package com.crowdaid.model.reward;

import com.crowdaid.model.common.BaseEntity;

/**
 * Reward class representing an item in the reward shop.
 * Donors can redeem rewards using earned credits.
 * 
 * Related to UC10 (Redeem Credits) and UC12 (Edit Reward Shop).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Reward extends BaseEntity {
    
    private String name;
    private String description;
    private double creditCost;
    private RewardCategory category;
    private int stock;
    private RewardStatus status;
    private String imagePath;
    
    /**
     * Default constructor.
     */
    public Reward() {
        super();
        this.status = RewardStatus.AVAILABLE;
        this.stock = 0;
    }
    
    /**
     * Constructor with essential fields.
     * 
     * @param name the reward name
     * @param description the reward description
     * @param creditCost the cost in credits
     * @param category the reward category
     * @param stock the available stock quantity
     */
    public Reward(String name, String description, double creditCost, RewardCategory category, int stock) {
        this();
        this.name = name;
        this.description = description;
        this.creditCost = creditCost;
        this.category = category;
        this.stock = stock;
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getCreditCost() {
        return creditCost;
    }
    
    public void setCreditCost(double creditCost) {
        this.creditCost = creditCost;
    }
    
    public RewardCategory getCategory() {
        return category;
    }
    
    public void setCategory(RewardCategory category) {
        this.category = category;
    }
    
    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
        if (stock <= 0) {
            this.status = RewardStatus.OUT_OF_STOCK;
        } else if (this.status == RewardStatus.OUT_OF_STOCK) {
            this.status = RewardStatus.AVAILABLE;
        }
    }
    
    public RewardStatus getStatus() {
        return status;
    }
    
    public void setStatus(RewardStatus status) {
        this.status = status;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    /**
     * Checks if the reward is available for redemption.
     * 
     * @return true if available and in stock
     */
    public boolean isAvailable() {
        return status == RewardStatus.AVAILABLE && stock > 0;
    }
    
    /**
     * Decreases stock by one when redeemed.
     * 
     * @return true if successful, false if out of stock
     */
    public boolean decrementStock() {
        if (stock > 0) {
            stock--;
            if (stock == 0) {
                status = RewardStatus.OUT_OF_STOCK;
            }
            this.touch();
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "Reward{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", creditCost=" + creditCost +
                ", category=" + category +
                ", stock=" + stock +
                ", status=" + status +
                '}';
    }
}
