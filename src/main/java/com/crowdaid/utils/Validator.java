package com.crowdaid.utils;

import com.crowdaid.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Validator utility class for input validation.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class Validator {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{10,15}$"
    );
    
    /**
     * Validates that a string is not null or empty.
     * 
     * @param value the value to validate
     * @param fieldName the field name for error messages
     * @throws ValidationException if validation fails
     */
    public static void validateNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }
    
    /**
     * Validates an email address format.
     * 
     * @param email the email to validate
     * @throws ValidationException if email format is invalid
     */
    public static void validateEmail(String email) throws ValidationException {
        validateNonEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }
    
    /**
     * Validates a password meets minimum requirements.
     * 
     * @param password the password to validate
     * @throws ValidationException if password is invalid
     */
    public static void validatePassword(String password) throws ValidationException {
        validateNonEmpty(password, "Password");
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }
    }
    
    /**
     * Validates that two passwords match.
     * 
     * @param password the password
     * @param confirmPassword the confirmation password
     * @throws ValidationException if passwords don't match
     */
    public static void validatePasswordMatch(String password, String confirmPassword) throws ValidationException {
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }
    }
    
    /**
     * Validates a phone number format.
     * 
     * @param phone the phone number to validate
     * @throws ValidationException if phone format is invalid
     */
    public static void validatePhone(String phone) throws ValidationException {
        if (phone != null && !phone.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone.replaceAll("[\\s-]", "")).matches()) {
                throw new ValidationException("Invalid phone number format");
            }
        }
    }
    
    /**
     * Validates that a number is positive.
     * 
     * @param value the value to validate
     * @param fieldName the field name for error messages
     * @throws ValidationException if value is not positive
     */
    public static void validatePositive(double value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero");
        }
    }
    
    /**
     * Validates that a number is non-negative.
     * 
     * @param value the value to validate
     * @param fieldName the field name for error messages
     * @throws ValidationException if value is negative
     */
    public static void validateNonNegative(double value, String fieldName) throws ValidationException {
        if (value < 0) {
            throw new ValidationException(fieldName + " cannot be negative");
        }
    }
    
    /**
     * Validates that a string length is within bounds.
     * 
     * @param value the value to validate
     * @param fieldName the field name for error messages
     * @param minLength minimum length
     * @param maxLength maximum length
     * @throws ValidationException if length is out of bounds
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) throws ValidationException {
        validateNonEmpty(value, fieldName);
        if (value.length() < minLength || value.length() > maxLength) {
            throw new ValidationException(fieldName + " must be between " + minLength + " and " + maxLength + " characters");
        }
    }
}
