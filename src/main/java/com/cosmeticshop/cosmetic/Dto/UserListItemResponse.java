package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class UserListItemResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private boolean accountLocked;
    private String status;
    private String statusReason;
    private LocalDateTime statusUpdatedAt;
    private String statusUpdatedBy;
    private int totalOrders;

    public UserListItemResponse() {
    }

    public UserListItemResponse(Long id, String username, String fullName, String email,
            String phone, String role, boolean accountLocked, String status,
            String statusReason, LocalDateTime statusUpdatedAt, String statusUpdatedBy,
            int totalOrders) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.accountLocked = accountLocked;
        this.status = status;
        this.statusReason = statusReason;
        this.statusUpdatedAt = statusUpdatedAt;
        this.statusUpdatedBy = statusUpdatedBy;
        this.totalOrders = totalOrders;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public LocalDateTime getStatusUpdatedAt() {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(LocalDateTime statusUpdatedAt) {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public String getStatusUpdatedBy() {
        return statusUpdatedBy;
    }

    public void setStatusUpdatedBy(String statusUpdatedBy) {
        this.statusUpdatedBy = statusUpdatedBy;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }
}