package com.cosmeticshop.cosmetic.Dto;

import jakarta.validation.constraints.NotNull;

public class UpdateUserLockRequest {

    @NotNull
    private Boolean accountLocked;

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }
}
