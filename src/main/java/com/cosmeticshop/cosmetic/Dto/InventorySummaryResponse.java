package com.cosmeticshop.cosmetic.Dto;

import java.util.List;

public class InventorySummaryResponse {

    private long totalProducts;
    private long totalStockUnits;
    private double totalInventoryValue;
    private long lowStockProducts;
    private long outOfStockProducts;
    private int lowStockThreshold;
    private List<RestockSuggestion> restockSuggestions;

    public InventorySummaryResponse() {
    }

    public InventorySummaryResponse(
            long totalProducts,
            long totalStockUnits,
            double totalInventoryValue,
            long lowStockProducts,
            long outOfStockProducts,
            int lowStockThreshold,
            List<RestockSuggestion> restockSuggestions) {
        this.totalProducts = totalProducts;
        this.totalStockUnits = totalStockUnits;
        this.totalInventoryValue = totalInventoryValue;
        this.lowStockProducts = lowStockProducts;
        this.outOfStockProducts = outOfStockProducts;
        this.lowStockThreshold = lowStockThreshold;
        this.restockSuggestions = restockSuggestions;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalStockUnits() {
        return totalStockUnits;
    }

    public void setTotalStockUnits(long totalStockUnits) {
        this.totalStockUnits = totalStockUnits;
    }

    public double getTotalInventoryValue() {
        return totalInventoryValue;
    }

    public void setTotalInventoryValue(double totalInventoryValue) {
        this.totalInventoryValue = totalInventoryValue;
    }

    public long getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(long lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public long getOutOfStockProducts() {
        return outOfStockProducts;
    }

    public void setOutOfStockProducts(long outOfStockProducts) {
        this.outOfStockProducts = outOfStockProducts;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public List<RestockSuggestion> getRestockSuggestions() {
        return restockSuggestions;
    }

    public void setRestockSuggestions(List<RestockSuggestion> restockSuggestions) {
        this.restockSuggestions = restockSuggestions;
    }

    public static class RestockSuggestion {
        private Long productId;
        private String productName;
        private int currentStock;
        private int recommendedRestockUnits;
        private double estimatedBudget;

        public RestockSuggestion() {
        }

        public RestockSuggestion(
                Long productId,
                String productName,
                int currentStock,
                int recommendedRestockUnits,
                double estimatedBudget) {
            this.productId = productId;
            this.productName = productName;
            this.currentStock = currentStock;
            this.recommendedRestockUnits = recommendedRestockUnits;
            this.estimatedBudget = estimatedBudget;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getCurrentStock() {
            return currentStock;
        }

        public void setCurrentStock(int currentStock) {
            this.currentStock = currentStock;
        }

        public int getRecommendedRestockUnits() {
            return recommendedRestockUnits;
        }

        public void setRecommendedRestockUnits(int recommendedRestockUnits) {
            this.recommendedRestockUnits = recommendedRestockUnits;
        }

        public double getEstimatedBudget() {
            return estimatedBudget;
        }

        public void setEstimatedBudget(double estimatedBudget) {
            this.estimatedBudget = estimatedBudget;
        }
    }
}
