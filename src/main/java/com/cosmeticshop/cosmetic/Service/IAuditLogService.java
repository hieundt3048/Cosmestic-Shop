package com.cosmeticshop.cosmetic.Service;

import java.util.List;

import com.cosmeticshop.cosmetic.Dto.AuditLogResponse;

public interface IAuditLogService {

    void logAction(String action, String target, String details);

    List<AuditLogResponse> getAuditLogs(String roleFilter, String query);
}
