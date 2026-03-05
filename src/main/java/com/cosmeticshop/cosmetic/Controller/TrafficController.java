package com.cosmeticshop.cosmetic.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Service.IActiveUserTrackingService;

@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

    private final IActiveUserTrackingService activeUserTrackingService;

    public TrafficController(IActiveUserTrackingService activeUserTrackingService) {
        this.activeUserTrackingService = activeUserTrackingService;
    }

    @PostMapping("/ping")
    public ResponseEntity<Map<String, String>> ping(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            activeUserTrackingService.markUserAsActive(authentication.getName());
        }
        return ResponseEntity.ok(Map.of("message", "Activity tracked"));
    }
}
