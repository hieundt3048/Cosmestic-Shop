package com.cosmeticshop.cosmetic.Dto;

import com.cosmeticshop.cosmetic.Entity.User;

import jakarta.validation.constraints.NotNull;

public class UpdateUserRoleRequest {

    @NotNull
    private User.Role role;

    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }
}
