package com.cosmeticshop.cosmetic.Dto;

import java.util.List;

public class FinancialReportResponse {

    private String range;
    private List<FinancialReportPointDto> points;
    private Double totalRevenue;
    private Double totalShipping;
    private Double totalTax;
    private Double totalNetProfit;

    public FinancialReportResponse() {
    }

    public FinancialReportResponse(
            String range,
            List<FinancialReportPointDto> points,
            Double totalRevenue,
            Double totalShipping,
            Double totalTax,
            Double totalNetProfit) {
        this.range = range;
        this.points = points;
        this.totalRevenue = totalRevenue;
        this.totalShipping = totalShipping;
        this.totalTax = totalTax;
        this.totalNetProfit = totalNetProfit;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public List<FinancialReportPointDto> getPoints() {
        return points;
    }

    public void setPoints(List<FinancialReportPointDto> points) {
        this.points = points;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getTotalShipping() {
        return totalShipping;
    }

    public void setTotalShipping(Double totalShipping) {
        this.totalShipping = totalShipping;
    }

    public Double getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Double totalTax) {
        this.totalTax = totalTax;
    }

    public Double getTotalNetProfit() {
        return totalNetProfit;
    }

    public void setTotalNetProfit(Double totalNetProfit) {
        this.totalNetProfit = totalNetProfit;
    }
}
