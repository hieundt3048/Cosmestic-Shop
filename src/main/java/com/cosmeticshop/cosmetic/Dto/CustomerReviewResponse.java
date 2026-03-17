package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class CustomerReviewResponse {

    private Long id;
    private Long productId;
    private Integer rating;
    private String comment;
    private String moderationStatus;
    private String message;
    private LocalDateTime createdAt;

    public CustomerReviewResponse() {
    }

    public CustomerReviewResponse(
            Long id,
            Long productId,
            Integer rating,
            String comment,
            String moderationStatus,
            String message,
            LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.moderationStatus = moderationStatus;
        this.message = message;
        this.createdAt = createdAt;
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

    public String getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
