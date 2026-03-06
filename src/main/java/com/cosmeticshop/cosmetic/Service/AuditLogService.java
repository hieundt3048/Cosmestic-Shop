package com.cosmeticshop.cosmetic.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.AuditLogResponse;
import com.cosmeticshop.cosmetic.Entity.AuditLog;
import com.cosmeticshop.cosmetic.Repository.AuditLogRepository;

@Service
public class AuditLogService implements IAuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Ghi 1 bản ghi audit cho thao tác vừa diễn ra trong hệ thống.
     * Actor (username/role) được lấy từ SecurityContext hiện tại.
     */
    @Override
    public void logAction(String action, String target, String details) {
        AuditLog log = new AuditLog();
        log.setActorUsername(getCurrentUsername());
        log.setActorRole(getCurrentRole());
        log.setAction(action);
        log.setTarget(target);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    /**
     * Lấy danh sách audit logs mới nhất và filter theo role/từ khóa.
     * - roleFilter: lọc theo vai trò người thao tác (ADMIN/EMPLOYEE...)
     * - query: tìm theo actor/action/target/details (không phân biệt hoa thường)
     */
    @Override
    public List<AuditLogResponse> getAuditLogs(String roleFilter, String query) {
        // Chuẩn hóa input để filter ổn định và tránh lỗi null
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String normalizedRole = roleFilter == null ? "" : roleFilter.trim().toUpperCase(Locale.ROOT);

        return auditLogRepository.findTop500ByOrderByCreatedAtDesc().stream()
                // Nếu không truyền role thì giữ nguyên tất cả
                .filter(log -> normalizedRole.isEmpty() || log.getActorRole().equalsIgnoreCase(normalizedRole))
                .filter(log -> {
                    if (normalizedQuery.isEmpty()) {
                        return true;
                    }
                    // Full-text đơn giản trên các trường thường tra cứu
                    return containsIgnoreCase(log.getActorUsername(), normalizedQuery)
                            || containsIgnoreCase(log.getAction(), normalizedQuery)
                            || containsIgnoreCase(log.getTarget(), normalizedQuery)
                            || containsIgnoreCase(log.getDetails(), normalizedQuery);
                })
                // Mapping sang DTO để trả dữ liệu gọn, không lộ entity trực tiếp
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getActorUsername(),
                        log.getActorRole(),
                        log.getAction(),
                        log.getTarget(),
                        log.getDetails(),
                        log.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Ưu tiên username từ phiên đăng nhập hiện tại.
     * Fallback "system" nếu thao tác không đi qua user context (job/background task).
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }

        String principalName = authentication.getName();
        return principalName == null || principalName.isBlank() ? "system" : principalName;
    }

    /**
     * Tách role từ authority có prefix ROLE_.
     * Ví dụ: ROLE_ADMIN -> ADMIN.
     */
    private String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getAuthorities() == null) {
            return "SYSTEM";
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String name = authority.getAuthority();
            if (name != null && name.startsWith("ROLE_")) {
                return name.substring("ROLE_".length());
            }
        }
        return "SYSTEM";
    }

    // Utility tìm chuỗi không phân biệt hoa thường và an toàn null.
    private boolean containsIgnoreCase(String source, String query) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(query);
    }
}
