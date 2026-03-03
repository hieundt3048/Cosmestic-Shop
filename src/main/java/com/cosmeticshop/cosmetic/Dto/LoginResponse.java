package com.cosmeticshop.cosmetic.Dto;

public class LoginResponse {

    private String token;
    private String message;
    private LoginUserDto user;

    public LoginResponse(){
    }

    public LoginResponse(String token, LoginUserDto user, String message){
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

    public LoginUserDto getUser(){
        return user;
    }

    public void setUser(LoginUserDto user){
        this.user = user;
    }
}
