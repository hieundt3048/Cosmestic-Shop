package com.cosmeticshop.cosmetic.Dto;

public class CreateOrderComplaintRequest {

    private String reason;
    private Double requestedRefundAmount;

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
}
