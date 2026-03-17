package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.SendSupportMessageRequest;
import com.cosmeticshop.cosmetic.Dto.SupportChatConversationResponse;
import com.cosmeticshop.cosmetic.Dto.SupportChatMessageResponse;
import com.cosmeticshop.cosmetic.Service.SupportChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/support-chat")
@PreAuthorize("hasAnyRole('CUSTOMER','EMPLOYEE')")
public class SupportChatController {

    private final SupportChatService supportChatService;

    public SupportChatController(SupportChatService supportChatService) {
        this.supportChatService = supportChatService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<SupportChatConversationResponse>> getMyConversations(Authentication authentication) {
        String username = authentication == null ? "" : authentication.getName();
        return ResponseEntity.ok(supportChatService.getMyConversations(username));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<SupportChatMessageResponse>> getConversationMessages(
            Authentication authentication,
            @RequestParam(required = false) Long withUserId) {
        String username = authentication == null ? "" : authentication.getName();
        return ResponseEntity.ok(supportChatService.getConversationMessages(username, withUserId));
    }

    @PostMapping("/messages")
    public ResponseEntity<SupportChatMessageResponse> sendMessage(
            Authentication authentication,
            @Valid @RequestBody SendSupportMessageRequest request) {
        String username = authentication == null ? "" : authentication.getName();
        return ResponseEntity.status(201).body(supportChatService.sendMessage(username, request));
    }
}
