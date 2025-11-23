package com.crowdaid.model.user;

import com.crowdaid.model.common.BaseEntity;

/**
 * BankInfo class representing a campaigner's bank account information.
 * Used for transferring released funds from escrow.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class BankInfo extends BaseEntity {
    
    private Long userId;
    private String accountHolderName;
    private String bankName;
    private String accountNumber;
    private String routingNumber;
    private String swiftCode;
    
    /**
     * Default constructor.
     */
    public BankInfo() {
        super();
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param id the bank info ID
     * @param userId the user's ID
     * @param accountHolderName the account holder's name
     * @param bankName the bank name
     * @param accountNumber the account number
     * @param routingNumber the routing number
     * @param swiftCode the SWIFT code
     */
    public BankInfo(Long id, Long userId, String accountHolderName, String bankName, 
                    String accountNumber, String routingNumber, String swiftCode) {
        super(id);
        this.userId = userId;
        this.accountHolderName = accountHolderName;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
        this.swiftCode = swiftCode;
    }
    
    // Getters and Setters
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getAccountHolderName() {
        return accountHolderName;
    }
    
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getRoutingNumber() {
        return routingNumber;
    }
    
    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }
    
    public String getSwiftCode() {
        return swiftCode;
    }
    
    public void setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }
    
    /**
     * Gets masked account number for display.
     * 
     * @return masked account number showing only last 4 digits
     */
    public String getMaskedAccountNumber() {
        if (accountNumber != null && accountNumber.length() >= 4) {
            return "****" + accountNumber.substring(accountNumber.length() - 4);
        }
        return "****";
    }
    
    @Override
    public String toString() {
        return "BankInfo{" +
                "id=" + id +
                ", userId=" + userId +
                ", accountHolderName='" + accountHolderName + '\'' +
                ", bankName='" + bankName + '\'' +
                ", maskedAccountNumber='" + getMaskedAccountNumber() + '\'' +
                '}';
    }
}
