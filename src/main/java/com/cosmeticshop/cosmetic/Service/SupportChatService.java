package com.cosmeticshop.cosmetic.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.SendSupportMessageRequest;
import com.cosmeticshop.cosmetic.Dto.SupportChatConversationResponse;
import com.cosmeticshop.cosmetic.Dto.SupportChatMessageResponse;
import com.cosmeticshop.cosmetic.Entity.SupportChatMessage;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Exception.ResourceNotFoundException;
import com.cosmeticshop.cosmetic.Repository.SupportChatMessageRepository;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class SupportChatService {

    private final SupportChatMessageRepository supportChatMessageRepository;
    private final UserRepository userRepository;
    private final IAuditLogService auditLogService;

    public SupportChatService(
            SupportChatMessageRepository supportChatMessageRepository,
            UserRepository userRepository,
            IAuditLogService auditLogService) {
        this.supportChatMessageRepository = supportChatMessageRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public List<SupportChatConversationResponse> getMyConversations(String username) {
        User currentUser = findByUsername(username);
        List<SupportChatMessage> recent = supportChatMessageRepository.findRecentForUser(currentUser.getId());

        // Dùng LinkedHashMap để vừa loại trùng theo otherUserId vừa giữ thứ tự recency.
        Map<Long, SupportChatConversationResponse> conversationMap = new LinkedHashMap<>();
        for (SupportChatMessage message : recent) {
            User other = message.getSender().getId().equals(currentUser.getId())
                    ? message.getRecipient()
                    : message.getSender();

            if (!canChat(currentUser, other)) {
                continue;
            }

            if (!conversationMap.containsKey(other.getId())) {
                // unreadCount chỉ tính chiều "other -> currentUser" và chưa được đọc.
                long unreadCount = recent.stream()
                        .filter(item -> item.getSender().getId().equals(other.getId()))
                        .filter(item -> item.getRecipient().getId().equals(currentUser.getId()))
                        .filter(item -> !item.isReadByRecipient())
                        .count();

                conversationMap.put(other.getId(), new SupportChatConversationResponse(
                        other.getId(),
                        displayName(other),
                        other.getRole().name(),
                        message.getContent(),
                        message.getCreatedAt(),
                        message.getSender().getId().equals(currentUser.getId()),
                        unreadCount));
            }
        }

        return conversationMap.values().stream().collect(Collectors.toList());
    }

    @Transactional
    public List<SupportChatMessageResponse> getConversationMessages(String username, Long withUserId) {
        User currentUser = findByUsername(username);
        User other = resolveConversationPeer(currentUser, withUserId);

        List<SupportChatMessage> messages = supportChatMessageRepository.findConversation(currentUser.getId(), other.getId());

        // Khi mở hội thoại, tự động mark các tin gửi tới currentUser là đã đọc.
        List<SupportChatMessage> unread = messages.stream()
                .filter(item -> item.getRecipient().getId().equals(currentUser.getId()))
                .filter(item -> !item.isReadByRecipient())
                .collect(Collectors.toList());

        unread.forEach(item -> item.setReadByRecipient(true));
        supportChatMessageRepository.saveAll(unread);

        return messages.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public SupportChatMessageResponse sendMessage(String username, SendSupportMessageRequest request) {
        User sender = findByUsername(username);
        User recipient = resolveMessageRecipient(sender, request.getRecipientId());

        if (!canChat(sender, recipient)) {
            throw new RuntimeException("Chỉ hỗ trợ chat giữa CUSTOMER và EMPLOYEE");
        }

        SupportChatMessage message = new SupportChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(request.getContent().trim());
        message.setReadByRecipient(false);

        SupportChatMessage saved = supportChatMessageRepository.save(message);
        auditLogService.logAction(
                "SUPPORT_CHAT_SEND_MESSAGE",
                "chat#" + saved.getId(),
                String.format("%s -> %s", sender.getUsername(), recipient.getUsername()));

        return toResponse(saved);
    }

    private User resolveMessageRecipient(User sender, Long recipientId) {
        if (sender.getRole() == User.Role.CUSTOMER) {
            // CUSTOMER không chỉ định recipient thì tự nối tới nhân viên mặc định.
            if (recipientId == null) {
                return findDefaultEmployee();
            }

            User target = userRepository.findById(recipientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người nhận với id: " + recipientId));
            if (target.getRole() != User.Role.EMPLOYEE) {
                throw new RuntimeException("Khách hàng chỉ có thể nhắn cho EMPLOYEE");
            }
            return target;
        }

        if (sender.getRole() == User.Role.EMPLOYEE) {
            // EMPLOYEE bắt buộc chọn đúng khách hàng để phản hồi.
            if (recipientId == null) {
                throw new RuntimeException("recipientId là bắt buộc khi nhân viên gửi tin");
            }

            User target = userRepository.findById(recipientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người nhận với id: " + recipientId));
            if (target.getRole() != User.Role.CUSTOMER) {
                throw new RuntimeException("Nhân viên chỉ có thể nhắn cho CUSTOMER");
            }
            return target;
        }

        throw new RuntimeException("Vai trò hiện tại không được phép chat hỗ trợ");
    }

    private User resolveConversationPeer(User currentUser, Long withUserId) {
        if (withUserId == null) {
            if (currentUser.getRole() == User.Role.CUSTOMER) {
                // CUSTOMER có thể vào luồng chat mà chưa chỉ định người nhận.
                return findDefaultEmployee();
            }
            throw new RuntimeException("withUserId là bắt buộc cho vai trò hiện tại");
        }

        User other = userRepository.findById(withUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với id: " + withUserId));

        if (!canChat(currentUser, other)) {
            throw new RuntimeException("Chỉ hỗ trợ chat giữa CUSTOMER và EMPLOYEE");
        }

        return other;
    }

    private User findDefaultEmployee() {
        return userRepository.findByRole(User.Role.EMPLOYEE).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Hiện chưa có nhân viên trực tuyến để hỗ trợ"));
    }

    private boolean canChat(User left, User right) {
        // Chính sách hiện tại chỉ cho phép hội thoại 1-1 giữa CUSTOMER và EMPLOYEE.
        return (left.getRole() == User.Role.CUSTOMER && right.getRole() == User.Role.EMPLOYEE)
                || (left.getRole() == User.Role.EMPLOYEE && right.getRole() == User.Role.CUSTOMER);
    }

    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));
    }

    private String displayName(User user) {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return user.getUsername();
        }
        return user.getFullName().trim();
    }

    private SupportChatMessageResponse toResponse(SupportChatMessage message) {
        return new SupportChatMessageResponse(
                message.getId(),
                message.getSender().getId(),
                displayName(message.getSender()),
                message.getSender().getRole().name(),
                message.getRecipient().getId(),
                displayName(message.getRecipient()),
                message.getRecipient().getRole().name(),
                message.getContent(),
                message.getCreatedAt(),
                message.isReadByRecipient());
    }
}
