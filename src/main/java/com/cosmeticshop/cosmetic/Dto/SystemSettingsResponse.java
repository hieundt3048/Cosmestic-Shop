package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class SystemSettingsResponse {

    private String shopName;
    private String supportPhone;
    private String supportEmail;
    private Double shippingFee;
    private String paymentMethods;

    private Integer minPasswordLength;
    private Integer sessionTimeoutMinutes;
    private Integer jwtExpirationMinutes;
    private Integer bcryptRounds;

    private Boolean autoBackupEnabled;
    private String backupOutputDir;
    private Integer backupIntervalHours;

    private LocalDateTime updatedAt;
    private String updatedBy;

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getSupportPhone() {
        return supportPhone;
    }

    public void setSupportPhone(String supportPhone) {
        this.supportPhone = supportPhone;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public Double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(String paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

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

    public Boolean getAutoBackupEnabled() {
        return autoBackupEnabled;
    }

    public void setAutoBackupEnabled(Boolean autoBackupEnabled) {
        this.autoBackupEnabled = autoBackupEnabled;
    }

    public String getBackupOutputDir() {
        return backupOutputDir;
    }

    public void setBackupOutputDir(String backupOutputDir) {
        this.backupOutputDir = backupOutputDir;
    }

    public Integer getBackupIntervalHours() {
        return backupIntervalHours;
    }

    public void setBackupIntervalHours(Integer backupIntervalHours) {
        this.backupIntervalHours = backupIntervalHours;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
