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

import com.cosmeticshop.cosmetic.Dto.EmployeeCancelOrderRequest;
import com.cosmeticshop.cosmetic.Dto.OrderSummaryResponse;
import com.cosmeticshop.cosmetic.Service.OrderManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders/employee")
@PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
public class EmployeeOrderController {

    private final OrderManagementService orderManagementService;

    public EmployeeOrderController(OrderManagementService orderManagementService) {
        this.orderManagementService = orderManagementService;
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getEmployeeOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(orderManagementService.getEmployeeOrders(status, q));
    }

    @PatchMapping("/{orderId}/advance")
    public ResponseEntity<OrderSummaryResponse> advanceOrderStatus(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderManagementService.advanceEmployeeOrderStatus(orderId));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderSummaryResponse> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody EmployeeCancelOrderRequest request) {
        return ResponseEntity.ok(orderManagementService.cancelOrderByEmployee(orderId, request.getReason()));
    }
}
