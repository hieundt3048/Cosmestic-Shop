package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.ProductResponse;
import com.cosmeticshop.cosmetic.Dto.UpdateProductExpiryRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateProductStockRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateProductVisibilityRequest;
import com.cosmeticshop.cosmetic.Service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products/employee")
@PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
public class EmployeeInventoryController {

    private final ProductService productService;

    public EmployeeInventoryController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getEmployeeInventory(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String visibility) {
        return ResponseEntity.ok(productService.getEmployeeInventory(q, brandId, categoryId, visibility));
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductStockRequest request) {
        return ResponseEntity.ok(productService.updateStockByEmployee(productId, request.getStockQuantity(), request.getReason()));
    }

    @PatchMapping("/{productId}/expiry")
    public ResponseEntity<ProductResponse> updateExpiry(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductExpiryRequest request) {
        return ResponseEntity.ok(productService.updateExpiryByEmployee(productId, request.getExpiryDate(), request.getNote()));
    }

    @PatchMapping("/{productId}/visibility")
    public ResponseEntity<ProductResponse> updateVisibility(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductVisibilityRequest request) {
        return ResponseEntity.ok(productService.updateVisibilityByEmployee(productId, request.getVisible(), request.getReason()));
    }
}
