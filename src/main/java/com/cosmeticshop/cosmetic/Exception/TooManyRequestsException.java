package com.cosmeticshop.cosmetic.Exception;

/**
 * TooManyRequestsException - Exception khi vượt quá số lần login cho phép
 * 
 * HTTP Status: 429 Too Many Requests
 */
public class TooManyRequestsException extends RuntimeException {
    
    private final long blockedMinutes;
    
    public TooManyRequestsException(String message, long blockedMinutes) {
        super(message);
        this.blockedMinutes = blockedMinutes;
    }
    
    public long getBlockedMinutes() {
        return blockedMinutes;
    }
}
