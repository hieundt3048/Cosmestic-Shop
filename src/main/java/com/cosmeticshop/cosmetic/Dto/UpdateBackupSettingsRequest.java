package com.cosmeticshop.cosmetic.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateBackupSettingsRequest {

    @NotNull
    private Boolean autoBackupEnabled;

    @NotBlank
    @Size(max = 500)
    private String backupOutputDir;

    @NotNull
    @Min(1)
    @Max(168)
    private Integer backupIntervalHours;

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
}
