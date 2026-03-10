package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDate;
import java.util.List;

public class OrderSummaryResponse {

    private Long id;
    private LocalDate orderDate;
    private Double totalAmount;
    private String status;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private Integer totalItems;
    private List<OrderItemSummaryResponse> items;

    public OrderSummaryResponse() {
    }

    public OrderSummaryResponse(
            Long id,
            LocalDate orderDate,
            Double totalAmount,
            String status,
            Long customerId,
            String customerName,
            String customerPhone,
            String shippingAddress,
            Integer totalItems,
            List<OrderItemSummaryResponse> items) {
        this.id = id;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.shippingAddress = shippingAddress;
        this.totalItems = totalItems;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public List<OrderItemSummaryResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemSummaryResponse> items) {
        this.items = items;
    }
}
