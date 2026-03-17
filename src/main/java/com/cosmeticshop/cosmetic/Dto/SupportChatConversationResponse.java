package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class SupportChatConversationResponse {

    private Long otherUserId;
    private String otherUserName;
    private String otherUserRole;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private boolean lastMessageFromMe;
    private long unreadCount;

    public SupportChatConversationResponse() {
    }

    public SupportChatConversationResponse(
            Long otherUserId,
            String otherUserName,
            String otherUserRole,
            String lastMessage,
            LocalDateTime lastMessageAt,
            boolean lastMessageFromMe,
            long unreadCount) {
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.otherUserRole = otherUserRole;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.lastMessageFromMe = lastMessageFromMe;
        this.unreadCount = unreadCount;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserRole() {
        return otherUserRole;
    }

    public void setOtherUserRole(String otherUserRole) {
        this.otherUserRole = otherUserRole;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public boolean isLastMessageFromMe() {
        return lastMessageFromMe;
    }

    public void setLastMessageFromMe(boolean lastMessageFromMe) {
        this.lastMessageFromMe = lastMessageFromMe;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
