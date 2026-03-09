package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.CreateOrderComplaintRequest;
import com.cosmeticshop.cosmetic.Dto.OrderComplaintResponse;
import com.cosmeticshop.cosmetic.Dto.OrderSummaryResponse;
import com.cosmeticshop.cosmetic.Dto.ResolveOrderComplaintRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateOrderStatusRequest;
import com.cosmeticshop.cosmetic.Service.OrderManagementService;

@RestController
@RequestMapping("/api/orders/admin")
public class OrderController {

    private final OrderManagementService orderManagementService;

    public OrderController(OrderManagementService orderManagementService) {
        this.orderManagementService = orderManagementService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderSummaryResponse>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(orderManagementService.getAllOrders(status, q));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderSummaryResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderManagementService.updateOrderStatus(orderId, request.getStatus(), request.getReason()));
    }

    @GetMapping("/complaints")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderComplaintResponse>> getComplaints(
            @RequestParam(required = false) String decision,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(orderManagementService.getComplaints(decision, q));
    }

    @PostMapping("/{orderId}/complaints")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderComplaintResponse> createComplaint(
            @PathVariable Long orderId,
            @RequestBody CreateOrderComplaintRequest request) {
        return ResponseEntity.status(201).body(orderManagementService.createComplaint(orderId, request));
    }

    @PatchMapping("/complaints/{complaintId}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderComplaintResponse> resolveComplaint(
            @PathVariable Long complaintId,
            @RequestBody ResolveOrderComplaintRequest request) {
        return ResponseEntity.ok(orderManagementService.resolveComplaint(complaintId, request));
    }
}
