package com.cosmeticshop.cosmetic.Entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, columnDefinition = "NVARCHAR(255)")
    private String username;

    @JsonIgnore  //Không trả password về client
    @Column(name = "password", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String password;

    @Column(name = "email", nullable = false, unique = true, columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "phone", nullable = false, unique = true, columnDefinition = "NVARCHAR(20)")
    private String phone;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Order> orders;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "NVARCHAR(50)")
    private Status status;

    @Column(name = "status_reason", columnDefinition = "NVARCHAR(500)")
    private String statusReason;

    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt;

    @Column(name = "status_updated_by", columnDefinition = "NVARCHAR(255)")
    private String statusUpdatedBy;

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public enum Role {
        ADMIN,
        EMPLOYEE,
        CUSTOMER
    }

    public enum Status {
        ACTIVE,
        LOCKED_MANUAL,
        POLICY_VIOLATION,
        FRAUD_SUSPECTED
    }

    // Getters and Setters

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
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
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
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
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
}
