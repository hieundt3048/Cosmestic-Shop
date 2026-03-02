package com.cosmeticshop.cosmetic.Dto;

import com.cosmeticshop.cosmetic.Entity.User;

public class RegisterResponse {
    
    private String message;
    private User user;
    private String token; 
    
    public RegisterResponse() {
    }
    
    public RegisterResponse(String message, User user) {
        this.message = message;
        this.user = user;
    }
    
    public RegisterResponse(String message, User user, String token) {
        this.message = message;
        this.user = user;
        this.token = token;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
}
