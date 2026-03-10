package com.cosmeticshop.cosmetic.Service;

import java.io.IOException;
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

@Service
/**
 * Quan ly backup SQL Server: trigger thu cong, scheduler dinh ky va luu lich su backup.
 *
 * Luong tong quat:
 * 1. Xac dinh DB name tu `spring.datasource.url`.
 * 2. Tao duong dan file .bak theo timestamp.
 * 3. Goi lenh native SQL Server: BACKUP DATABASE ... TO DISK.
 * 4. Luu ket qua thanh cong/that bai vao `backup_records` de FE hien thi.
 */
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
        // Chi lay 100 ban ghi moi nhat de trang admin load nhanh.
        return backupRecordRepository.findTop100ByOrderByStartedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Trigger backup thu cong tu nut "Tao ban sao luu ngay" tren FE.
     */
    public BackupRecordResponse triggerManualBackup() {
        // Khong dung @Transactional o luong backup vi SQL Server cam BACKUP trong transaction (error 3021).
        BackupRecord record = executeBackup(BackupRecord.TriggerType.MANUAL, getCurrentActorUsername());
        return toResponse(record);
    }

    /**
     * Scheduler check theo fixed delay (mac dinh 5 phut), nhung CHI thuc su backup khi den han.
     * Vi vay tick scheduler nho khong lam tang tan suat backup ngoai `backupIntervalHours`.
     */
    @Scheduled(fixedDelayString = "${system.backup.scheduler-interval-ms:300000}")
    public void runScheduledBackupIfDue() {
        // Scheduler van chay tren moi profile; skip som neu datasource khong phai SQL Server.
        if (!isSqlServerDatasource()) {
            return;
        }

        SystemSettings settings = systemSettingsService.getOrCreateSettings();
        if (!Boolean.TRUE.equals(settings.getAutoBackupEnabled())) {
            return;
        }

        Integer intervalHours = settings.getBackupIntervalHours();
        if (intervalHours == null || intervalHours < 1) {
            // Gia tri fallback an toan neu setting bi null/sai.
            intervalHours = 24;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(intervalHours);

        // Chi backup khi ban ghi scheduled gan nhat da qua nguong interval.
        boolean shouldRun = backupRecordRepository.findTopByTriggerTypeOrderByStartedAtDesc(BackupRecord.TriggerType.SCHEDULED)
                .map(record -> record.getStartedAt() == null || record.getStartedAt().isBefore(threshold))
                .orElse(true);

        if (!shouldRun) {
            return;
        }

        executeBackup(BackupRecord.TriggerType.SCHEDULED, "system");
    }

    private BackupRecord executeBackup(BackupRecord.TriggerType triggerType, String triggeredBy) {
        // settings duoc doc o moi lan chay de ap dung thay doi runtime ngay lap tuc.
        SystemSettings settings = systemSettingsService.getOrCreateSettings();
        String databaseName = resolveDatabaseName();

        BackupRecord record = new BackupRecord();
        record.setTriggerType(triggerType);
        record.setDatabaseName(databaseName);
        record.setTriggeredBy(triggeredBy);
        record.setStatus(BackupRecord.Status.FAILED);

        LocalDateTime startedAt = LocalDateTime.now();
        record.setStartedAt(startedAt);

        if (!isSqlServerDatasource()) {
            // Ghi nhan FAILED de dashboard hien ro ly do backup khong duoc ho tro tren datasource hien tai.
            LocalDateTime finishedAt = LocalDateTime.now();
            record.setFinishedAt(finishedAt);
            record.setDurationMs(Duration.between(startedAt, finishedAt).toMillis());
            record.setMessage("Backup command is only supported for SQL Server datasource");
            return backupRecordRepository.save(record);
        }

        try {
            String outputDir = StringUtils.hasText(settings.getBackupOutputDir()) ? settings.getBackupOutputDir().trim() : "backups";
            Path outputPath = Path.of(outputDir);
            // Tao thu muc neu chua ton tai (vd: backups/).
            Files.createDirectories(outputPath);

            String fileName = databaseName + "_" + FILE_TIME_FORMAT.format(startedAt) + ".bak";
            Path filePath = outputPath.resolve(fileName).toAbsolutePath();

            // Goi lenh native BACKUP DATABASE cua SQL Server.
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
        } catch (IOException | RuntimeException ex) {
            // Cac loi thuong gap:
            // - SQL Server account khong co quyen ghi vao thu muc dich.
            // - Duong dan file khong hop le tren may chu SQL Server.
            // - DB dang bi state khong cho phep backup.
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
        // Escaping don gian de tranh vo query khi ten DB/path chua ky tu dac biet.
        // Luu y: day la SQL command native, khong phai script xuat du lieu .sql.
        String safeDatabase = databaseName.replace("]", "");
        String safePath = filePath.replace("'", "''");
        return "BACKUP DATABASE [" + safeDatabase + "] TO DISK = N'" + safePath
                + "' WITH FORMAT, INIT, NAME = N'" + safeDatabase + "-Full Backup', SKIP, NOREWIND, NOUNLOAD, STATS = 10";
    }

    private String resolveDatabaseName() {
        // Parse gia tri databaseName trong JDBC URL, vi du: ...;databaseName=CosmeticDB;...
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

    private boolean isSqlServerDatasource() {
        // He thong chi ho tro backup native cho SQL Server.
        return datasourceUrl != null && datasourceUrl.toLowerCase(Locale.ROOT).contains("jdbc:sqlserver:");
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
