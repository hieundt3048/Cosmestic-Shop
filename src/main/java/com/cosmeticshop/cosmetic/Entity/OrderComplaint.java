package com.cosmeticshop.cosmetic.Entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_complaints")
public class OrderComplaint {

    public enum Decision {
        PENDING,
        REFUND_APPROVED,
        CANCEL_APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @Column(name = "reason", nullable = false, columnDefinition = "NVARCHAR(1000)")
    private String reason;

    @Column(name = "requested_refund_amount")
    private Double requestedRefundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 30)
    private Decision decision = Decision.PENDING;

    @Column(name = "approved_refund_amount")
    private Double approvedRefundAmount;

    @Column(name = "resolution_note", columnDefinition = "NVARCHAR(1000)")
    private String resolutionNote;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by", columnDefinition = "NVARCHAR(255)")
    private String resolvedBy;

    @PrePersist
    public void onCreate() {
        this.requestedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getRequestedRefundAmount() {
        return requestedRefundAmount;
    }

    public void setRequestedRefundAmount(Double requestedRefundAmount) {
        this.requestedRefundAmount = requestedRefundAmount;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public Double getApprovedRefundAmount() {
        return approvedRefundAmount;
    }

    public void setApprovedRefundAmount(Double approvedRefundAmount) {
        this.approvedRefundAmount = approvedRefundAmount;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }
}
