package com.cosmeticshop.cosmetic.Dto;

public class OrderKpiResponse {

    private String range;
    private Integer totalOrders;
    private Integer successfulOrders;
    private Integer canceledOrders;
    private Double conversionRate;

    public OrderKpiResponse(
            String range,
            Integer totalOrders,
            Integer successfulOrders,
            Integer canceledOrders,
            Double conversionRate) {
        this.range = range;
        this.totalOrders = totalOrders;
        this.successfulOrders = successfulOrders;
        this.canceledOrders = canceledOrders;
        this.conversionRate = conversionRate;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getSuccessfulOrders() {
        return successfulOrders;
    }

    public void setSuccessfulOrders(Integer successfulOrders) {
        this.successfulOrders = successfulOrders;
    }

    public Integer getCanceledOrders() {
        return canceledOrders;
    }

    public void setCanceledOrders(Integer canceledOrders) {
        this.canceledOrders = canceledOrders;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }
}
