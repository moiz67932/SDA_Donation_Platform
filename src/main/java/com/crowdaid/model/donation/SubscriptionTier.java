package com.crowdaid.model.donation;

/**
 * SubscriptionTier class representing a predefined subscription tier.
 * Contains tier information like name, monthly amount, and benefits.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class SubscriptionTier {
    
    private String name;
    private double monthlyAmount;
    private String description;
    private String benefits;
    
    /**
     * Default constructor.
     */
    public SubscriptionTier() {
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param name the tier name
     * @param monthlyAmount the monthly subscription amount
     * @param description the tier description
     */
    public SubscriptionTier(String name, double monthlyAmount, String description) {
        this.name = name;
        this.monthlyAmount = monthlyAmount;
        this.description = description;
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getMonthlyAmount() {
        return monthlyAmount;
    }
    
    public void setMonthlyAmount(double monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getBenefits() {
        return benefits;
    }
    
    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }
    
    /**
     * Gets predefined subscription tiers.
     * 
     * @return array of subscription tiers
     */
    public static SubscriptionTier[] getDefaultTiers() {
        return new SubscriptionTier[]{
            new SubscriptionTier("Bronze", 10.0, "Basic supporter tier with monthly updates"),
            new SubscriptionTier("Silver", 25.0, "Silver supporter with exclusive updates and recognition"),
            new SubscriptionTier("Gold", 50.0, "Gold supporter with premium benefits and early access")
        };
    }
    
    @Override
    public String toString() {
        return name + " - $" + monthlyAmount + "/month";
    }
}
