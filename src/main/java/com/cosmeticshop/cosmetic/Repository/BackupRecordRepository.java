package com.cosmeticshop.cosmetic.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cosmeticshop.cosmetic.Entity.BackupRecord;

@Repository
public interface BackupRecordRepository extends JpaRepository<BackupRecord, Long> {

    List<BackupRecord> findTop100ByOrderByStartedAtDesc();

    Optional<BackupRecord> findTopByTriggerTypeOrderByStartedAtDesc(BackupRecord.TriggerType triggerType);
}
