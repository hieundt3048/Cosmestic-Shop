package com.cosmeticshop.cosmetic.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cosmeticshop.cosmetic.Dto.BackupRecordResponse;
import com.cosmeticshop.cosmetic.Entity.BackupRecord;
import com.cosmeticshop.cosmetic.Entity.SystemSettings;
import com.cosmeticshop.cosmetic.Repository.BackupRecordRepository;

import jakarta.transaction.Transactional;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.ROOT);

    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final BackupRecordRepository backupRecordRepository;
    private final SystemSettingsService systemSettingsService;
    private final IAuditLogService auditLogService;
    private final String datasourceUrl;

    public BackupService(
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate,
            BackupRecordRepository backupRecordRepository,
            SystemSettingsService systemSettingsService,
            IAuditLogService auditLogService,
            @Value("${spring.datasource.url}") String datasourceUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.backupRecordRepository = backupRecordRepository;
        this.systemSettingsService = systemSettingsService;
        this.auditLogService = auditLogService;
        this.datasourceUrl = datasourceUrl;
    }

    public List<BackupRecordResponse> getBackupHistory() {
        return backupRecordRepository.findTop100ByOrderByStartedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BackupRecordResponse triggerManualBackup() {
        BackupRecord record = executeBackup(BackupRecord.TriggerType.MANUAL, getCurrentActorUsername());
        return toResponse(record);
    }

    @Scheduled(fixedDelayString = "${system.backup.scheduler-interval-ms:300000}")
    @Transactional
    public void runScheduledBackupIfDue() {
        SystemSettings settings = systemSettingsService.getOrCreateSettings();
        if (!Boolean.TRUE.equals(settings.getAutoBackupEnabled())) {
            return;
        }

        Integer intervalHours = settings.getBackupIntervalHours();
        if (intervalHours == null || intervalHours < 1) {
            intervalHours = 24;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(intervalHours);

        boolean shouldRun = backupRecordRepository.findTopByTriggerTypeOrderByStartedAtDesc(BackupRecord.TriggerType.SCHEDULED)
                .map(record -> record.getStartedAt() == null || record.getStartedAt().isBefore(threshold))
                .orElse(true);

        if (!shouldRun) {
            return;
        }

        executeBackup(BackupRecord.TriggerType.SCHEDULED, "system");
    }

    private BackupRecord executeBackup(BackupRecord.TriggerType triggerType, String triggeredBy) {
        SystemSettings settings = systemSettingsService.getOrCreateSettings();
        String databaseName = resolveDatabaseName();

        BackupRecord record = new BackupRecord();
        record.setTriggerType(triggerType);
        record.setDatabaseName(databaseName);
        record.setTriggeredBy(triggeredBy);
        record.setStatus(BackupRecord.Status.FAILED);

        LocalDateTime startedAt = LocalDateTime.now();
        record.setStartedAt(startedAt);

        try {
            String outputDir = StringUtils.hasText(settings.getBackupOutputDir()) ? settings.getBackupOutputDir().trim() : "backups";
            Path outputPath = Path.of(outputDir);
            Files.createDirectories(outputPath);

            String fileName = databaseName + "_" + FILE_TIME_FORMAT.format(startedAt) + ".bak";
            Path filePath = outputPath.resolve(fileName).toAbsolutePath();

            String backupSql = buildBackupSql(databaseName, filePath.toString());
            jdbcTemplate.execute(backupSql);

            LocalDateTime finishedAt = LocalDateTime.now();
            record.setFinishedAt(finishedAt);
            record.setDurationMs(Duration.between(startedAt, finishedAt).toMillis());
            record.setStatus(BackupRecord.Status.SUCCESS);
            record.setBackupFilePath(filePath.toString());
            record.setMessage("Backup completed successfully");

            BackupRecord saved = backupRecordRepository.save(record);
            auditLogService.logAction(
                    "DATABASE_BACKUP_" + triggerType.name(),
                    "database#" + databaseName,
                    "Backup thành công tới file: " + filePath);
            return saved;
        } catch (Exception ex) {
            LocalDateTime finishedAt = LocalDateTime.now();
            record.setFinishedAt(finishedAt);
            record.setDurationMs(Duration.between(startedAt, finishedAt).toMillis());
            record.setMessage(ex.getMessage());

            BackupRecord saved = backupRecordRepository.save(record);
            logger.error("Database backup failed ({}): {}", triggerType, ex.getMessage(), ex);
            auditLogService.logAction(
                    "DATABASE_BACKUP_FAILED",
                    "database#" + databaseName,
                    "Backup thất bại: " + ex.getMessage());
            return saved;
        }
    }

    private String buildBackupSql(String databaseName, String filePath) {
        String safeDatabase = databaseName.replace("]", "");
        String safePath = filePath.replace("'", "''");
        return "BACKUP DATABASE [" + safeDatabase + "] TO DISK = N'" + safePath
                + "' WITH FORMAT, INIT, NAME = N'" + safeDatabase + "-Full Backup', SKIP, NOREWIND, NOUNLOAD, STATS = 10";
    }

    private String resolveDatabaseName() {
        String marker = "databaseName=";
        int index = datasourceUrl.toLowerCase(Locale.ROOT).indexOf(marker.toLowerCase(Locale.ROOT));
        if (index < 0) {
            return "CosmeticDB";
        }

        String remainder = datasourceUrl.substring(index + marker.length());
        int separator = remainder.indexOf(';');
        String extracted = separator >= 0 ? remainder.substring(0, separator) : remainder;
        return extracted.isBlank() ? "CosmeticDB" : extracted;
    }

    private BackupRecordResponse toResponse(BackupRecord record) {
        BackupRecordResponse response = new BackupRecordResponse();
        response.setId(record.getId());
        response.setStatus(record.getStatus() == null ? null : record.getStatus().name());
        response.setTriggerType(record.getTriggerType() == null ? null : record.getTriggerType().name());
        response.setDatabaseName(record.getDatabaseName());
        response.setBackupFilePath(record.getBackupFilePath());
        response.setMessage(record.getMessage());
        response.setTriggeredBy(record.getTriggeredBy());
        response.setStartedAt(record.getStartedAt());
        response.setFinishedAt(record.getFinishedAt());
        response.setDurationMs(record.getDurationMs());
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
