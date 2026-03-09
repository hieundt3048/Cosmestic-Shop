package com.cosmeticshop.cosmetic.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.SystemSettingsResponse;
import com.cosmeticshop.cosmetic.Dto.UpdateBackupSettingsRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateGeneralSettingsRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateSecuritySettingsRequest;
import com.cosmeticshop.cosmetic.Entity.SystemSettings;
import com.cosmeticshop.cosmetic.Repository.SystemSettingsRepository;

import jakarta.transaction.Transactional;

@Service
public class SystemSettingsService {

    private static final long SINGLETON_ID = 1L;

    private final SystemSettingsRepository systemSettingsRepository;
    private final IAuditLogService auditLogService;

    @Value("${system.settings.default-shop-name:LUMIA Cosmetic Shop}")
    private String defaultShopName;

    @Value("${system.settings.default-support-phone:1900 1234}")
    private String defaultSupportPhone;

    @Value("${system.settings.default-support-email:support@lumia.vn}")
    private String defaultSupportEmail;

    @Value("${system.settings.default-shipping-fee:30000}")
    private Double defaultShippingFee;

    @Value("${system.settings.default-payment-methods:COD, BANK_TRANSFER}")
    private String defaultPaymentMethods;

    @Value("${system.backup.output-dir:backups}")
    private String defaultBackupOutputDir;

    public SystemSettingsService(SystemSettingsRepository systemSettingsRepository, IAuditLogService auditLogService) {
        this.systemSettingsRepository = systemSettingsRepository;
        this.auditLogService = auditLogService;
    }

    public SystemSettingsResponse getSettings() {
        return toResponse(getOrCreateSettings());
    }

    public SystemSettings getOrCreateSettings() {
        Optional<SystemSettings> existing = systemSettingsRepository.findById(SINGLETON_ID);
        if (existing.isPresent()) {
            return existing.get();
        }

        SystemSettings created = new SystemSettings();
        created.setId(SINGLETON_ID);
        created.setShopName(defaultShopName);
        created.setSupportPhone(defaultSupportPhone);
        created.setSupportEmail(defaultSupportEmail);
        created.setShippingFee(defaultShippingFee);
        created.setPaymentMethods(defaultPaymentMethods);

        created.setMinPasswordLength(8);
        created.setSessionTimeoutMinutes(120);
        created.setJwtExpirationMinutes(60);
        created.setBcryptRounds(10);

        created.setAutoBackupEnabled(Boolean.TRUE);
        created.setBackupOutputDir(defaultBackupOutputDir);
        created.setBackupIntervalHours(24);

        created.setUpdatedBy("system");
        return systemSettingsRepository.save(created);
    }

    @Transactional
    public SystemSettingsResponse updateGeneralSettings(UpdateGeneralSettingsRequest request) {
        SystemSettings settings = getOrCreateSettings();
        settings.setShopName(request.getShopName().trim());
        settings.setSupportPhone(request.getSupportPhone().trim());
        settings.setSupportEmail(request.getSupportEmail().trim());
        settings.setShippingFee(request.getShippingFee());
        settings.setPaymentMethods(request.getPaymentMethods().trim());
        settings.setUpdatedBy(getCurrentActorUsername());

        SystemSettings saved = systemSettingsRepository.save(settings);
        auditLogService.logAction(
                "UPDATE_SYSTEM_GENERAL_SETTINGS",
                "system_settings#" + saved.getId(),
                "Cập nhật cấu hình chung hệ thống");
        return toResponse(saved);
    }

    @Transactional
    public SystemSettingsResponse updateSecuritySettings(UpdateSecuritySettingsRequest request) {
        SystemSettings settings = getOrCreateSettings();
        settings.setMinPasswordLength(request.getMinPasswordLength());
        settings.setSessionTimeoutMinutes(request.getSessionTimeoutMinutes());
        settings.setJwtExpirationMinutes(request.getJwtExpirationMinutes());
        settings.setBcryptRounds(request.getBcryptRounds());
        settings.setUpdatedBy(getCurrentActorUsername());

        SystemSettings saved = systemSettingsRepository.save(settings);
        auditLogService.logAction(
                "UPDATE_SYSTEM_SECURITY_SETTINGS",
                "system_settings#" + saved.getId(),
                "Cập nhật chính sách bảo mật hệ thống");
        return toResponse(saved);
    }

    @Transactional
    public SystemSettingsResponse updateBackupSettings(UpdateBackupSettingsRequest request) {
        SystemSettings settings = getOrCreateSettings();
        settings.setAutoBackupEnabled(Boolean.TRUE.equals(request.getAutoBackupEnabled()));
        settings.setBackupOutputDir(request.getBackupOutputDir().trim());
        settings.setBackupIntervalHours(request.getBackupIntervalHours());
        settings.setUpdatedBy(getCurrentActorUsername());

        SystemSettings saved = systemSettingsRepository.save(settings);
        auditLogService.logAction(
                "UPDATE_SYSTEM_BACKUP_SETTINGS",
                "system_settings#" + saved.getId(),
                "Cập nhật cấu hình sao lưu hệ thống");
        return toResponse(saved);
    }

    private SystemSettingsResponse toResponse(SystemSettings settings) {
        SystemSettingsResponse response = new SystemSettingsResponse();
        response.setShopName(settings.getShopName());
        response.setSupportPhone(settings.getSupportPhone());
        response.setSupportEmail(settings.getSupportEmail());
        response.setShippingFee(settings.getShippingFee());
        response.setPaymentMethods(settings.getPaymentMethods());

        response.setMinPasswordLength(settings.getMinPasswordLength());
        response.setSessionTimeoutMinutes(settings.getSessionTimeoutMinutes());
        response.setJwtExpirationMinutes(settings.getJwtExpirationMinutes());
        response.setBcryptRounds(settings.getBcryptRounds());

        response.setAutoBackupEnabled(settings.getAutoBackupEnabled());
        response.setBackupOutputDir(settings.getBackupOutputDir());
        response.setBackupIntervalHours(settings.getBackupIntervalHours());

        response.setUpdatedAt(settings.getUpdatedAt());
        response.setUpdatedBy(settings.getUpdatedBy());
        return response;
    }

    private String getCurrentActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        String name = authentication.getName();
        return (name == null || name.isBlank()) ? "system" : name;
    }
}
