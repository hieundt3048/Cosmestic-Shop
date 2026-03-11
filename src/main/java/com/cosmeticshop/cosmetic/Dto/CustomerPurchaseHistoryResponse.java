package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDate;
import java.util.List;

public class CustomerPurchaseHistoryResponse {

    private Long customerId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private int totalOrders;
    private List<OrderHistoryItem> orders;

    public CustomerPurchaseHistoryResponse() {
    }

    public CustomerPurchaseHistoryResponse(
            Long customerId,
            String username,
            String fullName,
            String email,
            String phone,
            int totalOrders,
            List<OrderHistoryItem> orders) {
        this.customerId = customerId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.totalOrders = totalOrders;
        this.orders = orders;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public List<OrderHistoryItem> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderHistoryItem> orders) {
        this.orders = orders;
    }

    public static class OrderHistoryItem {

        private Long id;
        private LocalDate orderDate;
        private Double totalAmount;
        private String status;
        private String shippingAddress;
        private Integer totalItems;

        public OrderHistoryItem() {
        }

        public OrderHistoryItem(
                Long id,
                LocalDate orderDate,
                Double totalAmount,
                String status,
                String shippingAddress,
                Integer totalItems) {
            this.id = id;
            this.orderDate = orderDate;
            this.totalAmount = totalAmount;
            this.status = status;
            this.shippingAddress = shippingAddress;
            this.totalItems = totalItems;
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
    }
}
