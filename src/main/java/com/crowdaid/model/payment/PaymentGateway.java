package com.crowdaid.model.payment;

/**
 * PaymentGateway interface defining payment processing operations.
 * Implementations provide actual or simulated payment processing.
 * 
 * Related to UC7 (Make Donation) and UC8 (Subscribe).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface PaymentGateway {
    
    /**
     * Processes a payment transaction.
     * 
     * @param amount the amount to charge
     * @param reference the transaction reference
     * @return true if payment successful, false otherwise
     */
    boolean processPayment(double amount, String reference);
    
    /**
     * Processes a refund transaction.
     * 
     * @param amount the amount to refund
     * @param originalReference the original transaction reference
     * @return true if refund successful, false otherwise
     */
    boolean processRefund(double amount, String originalReference);
    
    /**
     * Validates payment method details.
     * 
     * @param paymentMethodInfo payment method information
     * @return true if valid, false otherwise
     */
    boolean validatePaymentMethod(String paymentMethodInfo);
}
