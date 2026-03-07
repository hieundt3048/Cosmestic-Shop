package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class VoucherResponse {

    private Long id;
    private String code;
    private Double discountPercent;
    private String scope;
    private boolean active;
    private ProductSummary product;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VoucherResponse() {
    }

    public VoucherResponse(
            Long id,
            String code,
            Double discountPercent,
            String scope,
            boolean active,
            ProductSummary product,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.scope = scope;
        this.active = active;
        this.product = product;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ProductSummary getProduct() {
        return product;
    }

    public void setProduct(ProductSummary product) {
        this.product = product;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class ProductSummary {
        private Long id;
        private String name;

        public ProductSummary() {
        }

        public ProductSummary(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
