package com.cosmeticshop.cosmetic.Dto;

public class FinancialReportPointDto {

    private String label;
    private Double revenue;
    private Double shipping;
    private Double tax;
    private Double netProfit;

    public FinancialReportPointDto() {
    }

    public FinancialReportPointDto(String label, Double revenue, Double shipping, Double tax, Double netProfit) {
        this.label = label;
        this.revenue = revenue;
        this.shipping = shipping;
        this.tax = tax;
        this.netProfit = netProfit;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }

    public Double getShipping() {
        return shipping;
    }

    public void setShipping(Double shipping) {
        this.shipping = shipping;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public Double getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(Double netProfit) {
        this.netProfit = netProfit;
    }
}
