package com.cosmeticshop.cosmetic.Dto;

public class ResolveOrderComplaintRequest {

    private String decision;
    private String resolutionNote;
    private Double approvedRefundAmount;

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public Double getApprovedRefundAmount() {
        return approvedRefundAmount;
    }

    public void setApprovedRefundAmount(Double approvedRefundAmount) {
        this.approvedRefundAmount = approvedRefundAmount;
    }
}
