package com.cosmeticshop.cosmetic.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_settings")
public class SystemSettings {

    @Id
    private Long id;

    @Column(name = "shop_name", columnDefinition = "NVARCHAR(255)")
    private String shopName;

    @Column(name = "support_phone", columnDefinition = "NVARCHAR(50)")
    private String supportPhone;

    @Column(name = "support_email", columnDefinition = "NVARCHAR(255)")
    private String supportEmail;

    @Column(name = "shipping_fee")
    private Double shippingFee;

    @Column(name = "payment_methods", columnDefinition = "NVARCHAR(1000)")
    private String paymentMethods;

    @Column(name = "min_password_length")
    private Integer minPasswordLength;

    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes;

    @Column(name = "jwt_expiration_minutes")
    private Integer jwtExpirationMinutes;

    @Column(name = "bcrypt_rounds")
    private Integer bcryptRounds;

    @Column(name = "auto_backup_enabled")
    private Boolean autoBackupEnabled;

    @Column(name = "backup_output_dir", columnDefinition = "NVARCHAR(500)")
    private String backupOutputDir;

    @Column(name = "backup_interval_hours")
    private Integer backupIntervalHours;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", columnDefinition = "NVARCHAR(255)")
    private String updatedBy;

    @PrePersist
    public void onCreate() {
        if (this.id == null) {
            this.id = 1L;
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
