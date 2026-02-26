package com.cosmeticshop.cosmetic.Dto;

import com.cosmeticshop.cosmetic.Entity.User;

public class LoginResponse {

    private String token;
    private String message;
    private User user;

    public LoginResponse(){
    }

    public LoginResponse(String token, User user, String message){
        this.token = token;
        this.user = user;
        this.message = message;
    }

    public String getToken(){
        return token;
    }

    public void setToken(String token){
        this.token = token;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public User getUser(){
        return user;
    }

    public void setUser(User user){
        this.user = user;
    }
}
