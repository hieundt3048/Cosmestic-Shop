package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.BackupRecordResponse;
import com.cosmeticshop.cosmetic.Dto.SystemSettingsResponse;
import com.cosmeticshop.cosmetic.Dto.UpdateBackupSettingsRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateGeneralSettingsRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateSecuritySettingsRequest;
import com.cosmeticshop.cosmetic.Service.BackupService;
import com.cosmeticshop.cosmetic.Service.SystemSettingsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/system-settings")
@PreAuthorize("hasRole('ADMIN')")
public class SystemSettingsController {

    private final SystemSettingsService systemSettingsService;
    private final BackupService backupService;

    public SystemSettingsController(SystemSettingsService systemSettingsService, BackupService backupService) {
        this.systemSettingsService = systemSettingsService;
        this.backupService = backupService;
    }

    @GetMapping
    public ResponseEntity<SystemSettingsResponse> getSettings() {
        return ResponseEntity.ok(systemSettingsService.getSettings());
    }

    @PatchMapping("/general")
    public ResponseEntity<SystemSettingsResponse> updateGeneralSettings(@Valid @RequestBody UpdateGeneralSettingsRequest request) {
        return ResponseEntity.ok(systemSettingsService.updateGeneralSettings(request));
    }

    @PatchMapping("/security")
    public ResponseEntity<SystemSettingsResponse> updateSecuritySettings(@Valid @RequestBody UpdateSecuritySettingsRequest request) {
        return ResponseEntity.ok(systemSettingsService.updateSecuritySettings(request));
    }

    @PatchMapping("/backup")
    public ResponseEntity<SystemSettingsResponse> updateBackupSettings(@Valid @RequestBody UpdateBackupSettingsRequest request) {
        return ResponseEntity.ok(systemSettingsService.updateBackupSettings(request));
    }

    @GetMapping("/backups/history")
    public ResponseEntity<List<BackupRecordResponse>> getBackupHistory() {
        return ResponseEntity.ok(backupService.getBackupHistory());
    }

    @PostMapping("/backups/run")
    public ResponseEntity<BackupRecordResponse> runBackupNow() {
        return ResponseEntity.ok(backupService.triggerManualBackup());
    }
}
