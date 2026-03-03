package com.cosmeticshop.cosmetic.Dto;

import java.util.List;

public class RevenueStatisticsResponse {

    private String range;
    private List<RevenuePointDto> points;
    private Double totalRevenue;

    public RevenueStatisticsResponse(String range, List<RevenuePointDto> points, Double totalRevenue) {
        this.range = range;
        this.points = points;
        this.totalRevenue = totalRevenue;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public List<RevenuePointDto> getPoints() {
        return points;
    }

    public void setPoints(List<RevenuePointDto> points) {
        this.points = points;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
