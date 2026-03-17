package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class SupportChatMessageResponse {

    private Long id;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private Long recipientId;
    private String recipientName;
    private String recipientRole;
    private String content;
    private LocalDateTime createdAt;
    private boolean readByRecipient;

    public SupportChatMessageResponse() {
    }

    public SupportChatMessageResponse(
            Long id,
            Long senderId,
            String senderName,
            String senderRole,
            Long recipientId,
            String recipientName,
            String recipientRole,
            String content,
            LocalDateTime createdAt,
            boolean readByRecipient) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.recipientRole = recipientRole;
        this.content = content;
        this.createdAt = createdAt;
        this.readByRecipient = readByRecipient;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientRole() {
        return recipientRole;
    }

    public void setRecipientRole(String recipientRole) {
        this.recipientRole = recipientRole;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isReadByRecipient() {
        return readByRecipient;
    }

    public void setReadByRecipient(boolean readByRecipient) {
        this.readByRecipient = readByRecipient;
    }
}
