package com.cosmeticshop.cosmetic.Dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateProductVisibilityRequest {

    @NotNull
    private Boolean visible;

    @Size(max = 500)
    private String reason;

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
