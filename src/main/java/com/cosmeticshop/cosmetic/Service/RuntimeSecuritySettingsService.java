package com.cosmeticshop.cosmetic.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Entity.SystemSettings;
import com.cosmeticshop.cosmetic.Repository.SystemSettingsRepository;

@Service
public class RuntimeSecuritySettingsService {

    private final SystemSettingsRepository systemSettingsRepository;
    private final long defaultJwtExpirationMs;

    public RuntimeSecuritySettingsService(
            SystemSettingsRepository systemSettingsRepository,
            @Value("${jwt.expiration:3600000}") long defaultJwtExpirationMs) {
        this.systemSettingsRepository = systemSettingsRepository;
        this.defaultJwtExpirationMs = defaultJwtExpirationMs;
    }

    public int getMinPasswordLength() {
        return loadSettings()
                .map(SystemSettings::getMinPasswordLength)
                .filter(value -> value != null && value >= 6 && value <= 64)
                .orElse(8);
    }

    public int getSessionTimeoutMinutes() {
        return loadSettings()
                .map(SystemSettings::getSessionTimeoutMinutes)
                .filter(value -> value != null && value >= 5 && value <= 1440)
                .orElse(120);
    }

    public int getBcryptRounds() {
        return loadSettings()
                .map(SystemSettings::getBcryptRounds)
                .filter(value -> value != null && value >= 8 && value <= 15)
                .orElse(10);
    }

    public long getJwtExpirationMs() {
        return loadSettings()
                .map(SystemSettings::getJwtExpirationMinutes)
                .filter(value -> value != null && value >= 15 && value <= 10080)
                .map(value -> value.longValue() * 60_000L)
                .orElse(defaultJwtExpirationMs);
    }

    private java.util.Optional<SystemSettings> loadSettings() {
        return systemSettingsRepository.findById(1L);
    }
}
