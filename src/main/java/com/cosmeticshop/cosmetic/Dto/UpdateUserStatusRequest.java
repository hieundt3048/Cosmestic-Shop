package com.cosmeticshop.cosmetic.Dto;

import com.cosmeticshop.cosmetic.Entity.User;

import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusRequest {

    @NotNull
    private User.Status status;

    private String reason;

    public User.Status getStatus() {
        return status;
    }

    public void setStatus(User.Status status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
