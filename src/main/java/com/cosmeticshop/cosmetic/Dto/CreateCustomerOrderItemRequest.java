package com.cosmeticshop.cosmetic.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateCustomerOrderItemRequest {

    @NotNull(message = "productId không được để trống")
    private Long productId;

    @NotNull(message = "quantity không được để trống")
    @Min(value = 1, message = "Số lượng sản phẩm phải lớn hơn 0")
    private Integer quantity;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
