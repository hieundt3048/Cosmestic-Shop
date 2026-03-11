package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class PublicReviewResponse {

    private Long id;
    private String customerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public PublicReviewResponse() {
    }

    public PublicReviewResponse(Long id, String customerName, Integer rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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
}
