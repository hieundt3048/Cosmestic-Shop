package com.cosmeticshop.cosmetic.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Entity.SystemSettings;
import com.cosmeticshop.cosmetic.Repository.SystemSettingsRepository;

@Service
/**
 * Cung cap cau hinh bao mat runtime cho cac module:
 * - Password validator (min length)
 * - Active session window
 * - BCrypt cost factor
 * - JWT expiration
 *
 * Nguyen tac:
 * - Uu tien gia tri trong bang system_settings (id=1)
 * - Neu null/sai range thi fallback ve gia tri an toan mac dinh
 */
public class RuntimeSecuritySettingsService {

    private static final long SETTINGS_ID = 1L;

    private final SystemSettingsRepository systemSettingsRepository;
    private final long defaultJwtExpirationMs;

    public RuntimeSecuritySettingsService(
            SystemSettingsRepository systemSettingsRepository,
            @Value("${jwt.expiration:3600000}") long defaultJwtExpirationMs) {
        this.systemSettingsRepository = systemSettingsRepository;
        this.defaultJwtExpirationMs = defaultJwtExpirationMs;
    }

    public int getMinPasswordLength() {
        // Range hop le: 6..64. Ngoai khoang -> fallback 8.
        return loadSettings()
                .map(SystemSettings::getMinPasswordLength)
                .filter(value -> value != null && value >= 6 && value <= 64)
                .orElse(8);
    }

    public int getSessionTimeoutMinutes() {
        // Range hop le: 5..1440 phut (toi da 24h). Ngoai khoang -> fallback 120.
        return loadSettings()
                .map(SystemSettings::getSessionTimeoutMinutes)
                .filter(value -> value != null && value >= 5 && value <= 1440)
                .orElse(120);
    }

    public int getBcryptRounds() {
        // Range khuyen nghi cho BCrypt: 8..15. Ngoai khoang -> fallback 10.
        return loadSettings()
                .map(SystemSettings::getBcryptRounds)
                .filter(value -> value != null && value >= 8 && value <= 15)
                .orElse(10);
    }

    public long getJwtExpirationMs() {
        // Gia tri trong DB dang la "minutes", can doi sang milliseconds de JwtUtil su dung.
        // Range hop le: 15..10080 phut (toi da 7 ngay).
        return loadSettings()
                .map(SystemSettings::getJwtExpirationMinutes)
                .filter(value -> value != null && value >= 15 && value <= 10080)
                .map(value -> value.longValue() * 60_000L)
                .orElse(defaultJwtExpirationMs);
    }

    private java.util.Optional<SystemSettings> loadSettings() {
        // He thong su dung singleton row cho system settings.
        return systemSettingsRepository.findById(SETTINGS_ID);
    }
}
