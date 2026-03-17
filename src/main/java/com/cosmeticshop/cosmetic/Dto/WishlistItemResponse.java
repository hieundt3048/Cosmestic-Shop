package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class WishlistItemResponse {

    private Long id;
    private Long productId;
    private String name;
    private String brandName;
    private Double price;
    private String imageUrl;
    private Integer stockQuantity;
    private LocalDateTime addedAt;

    public WishlistItemResponse() {
    }

    public WishlistItemResponse(
            Long id,
            Long productId,
            String name,
            String brandName,
            Double price,
            String imageUrl,
            Integer stockQuantity,
            LocalDateTime addedAt) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.brandName = brandName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.stockQuantity = stockQuantity;
        this.addedAt = addedAt;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
