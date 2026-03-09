package com.cosmeticshop.cosmetic.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateSecuritySettingsRequest {

    @NotNull
    @Min(6)
    @Max(64)
    private Integer minPasswordLength;

    @NotNull
    @Min(5)
    @Max(1440)
    private Integer sessionTimeoutMinutes;

    @NotNull
    @Min(15)
    @Max(10080)
    private Integer jwtExpirationMinutes;

    @NotNull
    @Min(8)
    @Max(15)
    private Integer bcryptRounds;

    public Integer getMinPasswordLength() {
        return minPasswordLength;
    }

    public void setMinPasswordLength(Integer minPasswordLength) {
        this.minPasswordLength = minPasswordLength;
    }

    public Integer getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    public Integer getJwtExpirationMinutes() {
        return jwtExpirationMinutes;
    }

    public void setJwtExpirationMinutes(Integer jwtExpirationMinutes) {
        this.jwtExpirationMinutes = jwtExpirationMinutes;
    }

    public Integer getBcryptRounds() {
        return bcryptRounds;
    }

    public void setBcryptRounds(Integer bcryptRounds) {
        this.bcryptRounds = bcryptRounds;
    }
}
