package com.cosmeticshop.cosmetic.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.NotificationResponse;
import com.cosmeticshop.cosmetic.Service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasAnyRole('CUSTOMER','EMPLOYEE','ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        String username = authentication == null ? "" : authentication.getName();
        return ResponseEntity.ok(notificationService.getMyNotifications(username));
    }

    @GetMapping("/my/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String username = authentication == null ? "" : authentication.getName();
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(username)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            Authentication authentication,
            @PathVariable Long id) {
        String username = authentication == null ? "" : authentication.getName();
        return ResponseEntity.ok(notificationService.markAsRead(username, id));
    }

    @PatchMapping("/my/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String username = authentication == null ? "" : authentication.getName();
        notificationService.markAllAsRead(username);
        return ResponseEntity.noContent().build();
    }
}
