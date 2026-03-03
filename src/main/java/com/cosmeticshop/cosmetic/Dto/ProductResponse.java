package com.cosmeticshop.cosmetic.Dto;

public class ProductResponse {

    private Long id;
    private String name;
    private String productName;
    private Double price;
    private String description;
    private String imageUrl;
    private Integer stockQuantity;
    private BrandSummary brand;
    private CategorySummary category;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, Double price, String description, String imageUrl,
            Integer stockQuantity, BrandSummary brand, CategorySummary category) {
        this.id = id;
        this.name = name;
        this.productName = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.stockQuantity = stockQuantity;
        this.brand = brand;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.productName = name;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
        if (this.name == null) {
            this.name = productName;
        }
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BrandSummary getBrand() {
        return brand;
    }

    public void setBrand(BrandSummary brand) {
        this.brand = brand;
    }

    public CategorySummary getCategory() {
        return category;
    }

    public void setCategory(CategorySummary category) {
        this.category = category;
    }

    public static class BrandSummary {
        private Long id;
        private String name;
        private String origin;

        public BrandSummary() {
        }

        public BrandSummary(Long id, String name, String origin) {
            this.id = id;
            this.name = name;
            this.origin = origin;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }
    }

    public static class CategorySummary {
        private Long id;
        private String name;

        public CategorySummary() {
        }

        public CategorySummary(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}