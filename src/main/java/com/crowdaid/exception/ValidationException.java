package com.crowdaid.exception;

/**
 * ValidationException thrown when input validation fails.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class ValidationException extends Exception {
    
    /**
     * Constructor with message.
     * 
     * @param message the error message
     */
    public ValidationException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
