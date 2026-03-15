package com.cosmeticshop.cosmetic.Dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateCustomerOrderRequest {

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 500, message = "Địa chỉ giao hàng không được quá 500 ký tự")
    private String shippingAddress;

    @NotBlank(message = "Số điện thoại giao hàng không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và có 10 số)")
    private String shippingPhone;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

    @Valid
    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    private List<CreateCustomerOrderItemRequest> items;

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<CreateCustomerOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CreateCustomerOrderItemRequest> items) {
        this.items = items;
    }
}
