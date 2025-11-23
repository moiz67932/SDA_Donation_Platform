package com.crowdaid.model.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

/**
 * SimulatedPaymentGateway provides a simulated payment processing implementation.
 * Always returns success for development and testing purposes.
 * In production, this would be replaced with actual payment gateway integration.
 * 
 * Related to UC7 (Make Donation) and UC8 (Subscribe).
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class SimulatedPaymentGateway implements PaymentGateway {
    
    private static final Logger logger = LoggerFactory.getLogger(SimulatedPaymentGateway.class);
    private final Random random;
    private final double successRate;
    
    /**
     * Constructor with default 100% success rate.
     */
    public SimulatedPaymentGateway() {
        this(1.0);
    }
    
    /**
     * Constructor with configurable success rate.
     * 
     * @param successRate the probability of payment success (0.0 to 1.0)
     */
    public SimulatedPaymentGateway(double successRate) {
        this.random = new Random();
        this.successRate = Math.max(0.0, Math.min(1.0, successRate));
    }
    
    /**
     * Simulates payment processing.
     * 
     * @param amount the amount to charge
     * @param reference the transaction reference
     * @return true if payment successful (based on success rate)
     */
    @Override
    public boolean processPayment(double amount, String reference) {
        logger.info("Processing simulated payment: amount=${}, reference={}", amount, reference);
        
        // Simulate processing delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Validate amount
        if (amount <= 0) {
            logger.error("Invalid payment amount: {}", amount);
            return false;
        }
        
        // Simulate success/failure based on success rate
        boolean success = random.nextDouble() < successRate;
        
        if (success) {
            String transactionId = UUID.randomUUID().toString();
            logger.info("Payment successful: transactionId={}, amount=${}", transactionId, amount);
        } else {
            logger.warn("Payment failed: reference={}, amount=${}", reference, amount);
        }
        
        return success;
    }
    
    /**
     * Simulates refund processing.
     * 
     * @param amount the amount to refund
     * @param originalReference the original transaction reference
     * @return true if refund successful
     */
    @Override
    public boolean processRefund(double amount, String originalReference) {
        logger.info("Processing simulated refund: amount=${}, originalReference={}", amount, originalReference);
        
        // Simulate processing delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Validate amount
        if (amount <= 0) {
            logger.error("Invalid refund amount: {}", amount);
            return false;
        }
        
        String refundId = UUID.randomUUID().toString();
        logger.info("Refund successful: refundId={}, amount=${}", refundId, amount);
        return true;
    }
    
    /**
     * Simulates payment method validation.
     * Always returns true in simulation.
     * 
     * @param paymentMethodInfo payment method information
     * @return true (always valid in simulation)
     */
    @Override
    public boolean validatePaymentMethod(String paymentMethodInfo) {
        logger.debug("Validating simulated payment method: {}", paymentMethodInfo);
        return true;
    }
}
