package com.cosmeticshop.cosmetic.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cosmeticshop.cosmetic.Entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop500ByOrderByCreatedAtDesc();
}
