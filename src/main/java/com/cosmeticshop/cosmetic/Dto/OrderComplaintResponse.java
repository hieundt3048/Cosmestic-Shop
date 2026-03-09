package com.cosmeticshop.cosmetic.Dto;

import java.time.LocalDateTime;

public class OrderComplaintResponse {

    private Long id;
    private Long orderId;
    private String customerName;
    private String reason;
    private Double requestedRefundAmount;
    private String decision;
    private Double approvedRefundAmount;
    private String resolutionNote;
    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;

    public OrderComplaintResponse() {
    }

    public OrderComplaintResponse(
            Long id,
            Long orderId,
            String customerName,
            String reason,
            Double requestedRefundAmount,
            String decision,
            Double approvedRefundAmount,
            String resolutionNote,
            LocalDateTime requestedAt,
            LocalDateTime resolvedAt,
            String resolvedBy) {
        this.id = id;
        this.orderId = orderId;
        this.customerName = customerName;
        this.reason = reason;
        this.requestedRefundAmount = requestedRefundAmount;
        this.decision = decision;
        this.approvedRefundAmount = approvedRefundAmount;
        this.resolutionNote = resolutionNote;
        this.requestedAt = requestedAt;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
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
