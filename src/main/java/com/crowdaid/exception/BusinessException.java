package com.crowdaid.exception;

/**
 * BusinessException thrown when business logic validation fails.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class BusinessException extends Exception {
    
    /**
     * Constructor with message.
     * 
     * @param message the error message
     */
    public BusinessException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
