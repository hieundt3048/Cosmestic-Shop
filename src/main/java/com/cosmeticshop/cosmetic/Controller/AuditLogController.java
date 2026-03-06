package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.AuditLogResponse;
import com.cosmeticshop.cosmetic.Service.IAuditLogService;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final IAuditLogService auditLogService;

    public AuditLogController(IAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(auditLogService.getAuditLogs(role, q));
    }
}
