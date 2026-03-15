package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.CreateCustomerOrderRequest;
import com.cosmeticshop.cosmetic.Dto.OrderSummaryResponse;
import com.cosmeticshop.cosmetic.Service.OrderManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerOrderController {

    private final OrderManagementService orderManagementService;

    public CustomerOrderController(OrderManagementService orderManagementService) {
        this.orderManagementService = orderManagementService;
    }

    @PostMapping
    public ResponseEntity<OrderSummaryResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateCustomerOrderRequest request) {
        String username = authentication == null ? "" : authentication.getName();
        return ResponseEntity.status(201).body(orderManagementService.createOrderByCustomer(username, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderSummaryResponse>> getMyOrders(Authentication authentication) {
        String username = authentication == null ? "" : authentication.getName();
        return ResponseEntity.ok(orderManagementService.getMyOrders(username));
    }
}
