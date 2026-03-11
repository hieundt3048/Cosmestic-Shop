package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class EmployeeReviewResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String moderationStatus;
    private String moderationReason;
    private LocalDateTime moderatedAt;
    private String moderatedBy;

    public EmployeeReviewResponse() {
    }

    public EmployeeReviewResponse(
            Long id,
            Long productId,
            String productName,
            Long customerId,
            String customerName,
            String customerEmail,
            String customerPhone,
            Integer rating,
            String comment,
            LocalDateTime createdAt,
            String moderationStatus,
            String moderationReason,
            LocalDateTime moderatedAt,
            String moderatedBy) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.moderationStatus = moderationStatus;
        this.moderationReason = moderationReason;
        this.moderatedAt = moderatedAt;
        this.moderatedBy = moderatedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public String getModerationReason() {
        return moderationReason;
    }

    public void setModerationReason(String moderationReason) {
        this.moderationReason = moderationReason;
    }

    public LocalDateTime getModeratedAt() {
        return moderatedAt;
    }

    public void setModeratedAt(LocalDateTime moderatedAt) {
        this.moderatedAt = moderatedAt;
    }

    public String getModeratedBy() {
        return moderatedBy;
    }

    public void setModeratedBy(String moderatedBy) {
        this.moderatedBy = moderatedBy;
    }
}
