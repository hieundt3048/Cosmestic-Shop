package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.CreateProductRequest;
import com.cosmeticshop.cosmetic.Dto.InventorySummaryResponse;
import com.cosmeticshop.cosmetic.Dto.ProductResponse;
import com.cosmeticshop.cosmetic.Dto.PublicReviewResponse;
import com.cosmeticshop.cosmetic.Dto.UpdateProductRequest;
import com.cosmeticshop.cosmetic.Service.ProductService;
import com.cosmeticshop.cosmetic.Service.ReviewService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    public ProductController(ProductService productService, ReviewService reviewService) {
        this.productService = productService;
        this.reviewService = reviewService;
    }

    // Tạo sản phẩm mới (giữ endpoint cũ để tương thích FE hiện tại)
    @PostMapping("/create_product")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse createProduct(@RequestBody CreateProductRequest request) {
        return productService.createProduct(request);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse createProductRestful(@RequestBody CreateProductRequest request) {
        return productService.createProduct(request);
    }
    
    // Lấy tất cả sản phẩm
    @GetMapping
    public List<ProductResponse> getAllProduct() {
        return productService.getAllProducts();
    }

    @GetMapping("/new-arrivals")
    public List<ProductResponse> getNewArrivals(
            @RequestParam(defaultValue = "8") Integer limit) {
        return productService.getNewArrivals(limit);
    }

    @GetMapping("/best-sellers")
    public List<ProductResponse> getBestSellers(
            @RequestParam(defaultValue = "8") Integer limit) {
        return productService.getBestSellers(limit);
    }

    // Lấy sản phẩm theo id
    @GetMapping("/{id}")
    public ProductResponse getProductbyId(@PathVariable Long id) {

        return productService.getProductById(id);
    }

    @GetMapping("/{id}/reviews")
    public List<PublicReviewResponse> getPublicReviewsByProduct(@PathVariable Long id) {
        return reviewService.getPublicReviewsByProduct(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(@PathVariable Long id, @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @GetMapping("/inventory/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public InventorySummaryResponse getInventorySummary(
            @RequestParam(defaultValue = "10") Integer lowStockThreshold) {
        return productService.getInventorySummary(lowStockThreshold);
    }
    
    // Tìm kiếm sản phẩm
    @GetMapping("/search")
    public List<ProductResponse> searchMethod(@RequestParam String query) {
        return productService.searchProduct(query);
    }
    
}
