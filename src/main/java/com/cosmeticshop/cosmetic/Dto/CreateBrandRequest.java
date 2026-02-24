package com.cosmeticshop.cosmetic.Dto;

public class CreateBrandRequest {
    private String name;
    private String origin;

    
    public CreateBrandRequest() {
    }

    public CreateBrandRequest(String name, String origin) {
        this.name = name;
        this.origin = origin;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
