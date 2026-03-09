package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateOrderComplaintRequest;
import com.cosmeticshop.cosmetic.Dto.OrderComplaintResponse;
import com.cosmeticshop.cosmetic.Dto.OrderItemSummaryResponse;
import com.cosmeticshop.cosmetic.Dto.OrderSummaryResponse;
import com.cosmeticshop.cosmetic.Dto.ResolveOrderComplaintRequest;
import com.cosmeticshop.cosmetic.Entity.Order;
import com.cosmeticshop.cosmetic.Entity.OrderComplaint;
import com.cosmeticshop.cosmetic.Exception.ResourceNotFoundException;
import com.cosmeticshop.cosmetic.Repository.OrderComplaintRepository;
import com.cosmeticshop.cosmetic.Repository.OrderRepository;

@Service
public class OrderManagementService {

    private final OrderRepository orderRepository;
    private final OrderComplaintRepository orderComplaintRepository;
    private final IAuditLogService auditLogService;

    public OrderManagementService(
            OrderRepository orderRepository,
            OrderComplaintRepository orderComplaintRepository,
            IAuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.orderComplaintRepository = orderComplaintRepository;
        this.auditLogService = auditLogService;
    }

    public List<OrderSummaryResponse> getAllOrders(String status, String query) {
        String normalizedStatus = normalize(status);
        String normalizedQuery = normalize(query);

        return orderRepository.findAll().stream()
                .filter(order -> filterByStatus(order, normalizedStatus))
                .filter(order -> filterByQuery(order, normalizedQuery))
                .sorted((a, b) -> {
                    LocalDateTime left = a.getOrderDate() == null ? LocalDateTime.MIN : a.getOrderDate().atStartOfDay();
                    LocalDateTime right = b.getOrderDate() == null ? LocalDateTime.MIN : b.getOrderDate().atStartOfDay();
                    return right.compareTo(left);
                })
                .map(this::toOrderSummary)
                .collect(Collectors.toList());
    }

    public OrderSummaryResponse updateOrderStatus(Long orderId, String rawStatus, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với id: " + orderId));

        Order.Status nextStatus = parseOrderStatus(rawStatus);
        Order.Status oldStatus = order.getStatus();
        order.setStatus(nextStatus);

        Order saved = orderRepository.save(order);
        auditLogService.logAction(
                "ADMIN_UPDATE_ORDER_STATUS",
                "ORDER#" + orderId,
                "Status: " + safeStatusName(oldStatus) + " -> " + safeStatusName(nextStatus)
                        + (reason == null || reason.trim().isEmpty() ? "" : (" | reason=" + reason.trim())));

        return toOrderSummary(saved);
    }

    public List<OrderComplaintResponse> getComplaints(String decision, String query) {
        String normalizedDecision = normalize(decision);
        String normalizedQuery = normalize(query);

        return orderComplaintRepository.findAllByOrderByRequestedAtDesc().stream()
                .filter(item -> normalizedDecision.isEmpty() || item.getDecision().name().equalsIgnoreCase(normalizedDecision))
                .filter(item -> {
                    if (normalizedQuery.isEmpty()) {
                        return true;
                    }
                    String customerName = resolveCustomerName(item.getOrder());
                    return String.valueOf(item.getOrder().getId()).contains(normalizedQuery)
                            || containsIgnoreCase(customerName, normalizedQuery)
                            || containsIgnoreCase(item.getReason(), normalizedQuery);
                })
                .map(this::toComplaintResponse)
                .collect(Collectors.toList());
    }

    public OrderComplaintResponse createComplaint(Long orderId, CreateOrderComplaintRequest request) {
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new RuntimeException("Lý do khiếu nại không được để trống");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với id: " + orderId));

        OrderComplaint complaint = new OrderComplaint();
        complaint.setOrder(order);
        complaint.setReason(request.getReason().trim());
        complaint.setRequestedRefundAmount(request.getRequestedRefundAmount());

        OrderComplaint saved = orderComplaintRepository.save(complaint);
        auditLogService.logAction(
                "ADMIN_CREATE_ORDER_COMPLAINT",
                "ORDER#" + orderId,
                "Tạo hồ sơ khiếu nại/hoàn tiền cho đơn hàng");
        return toComplaintResponse(saved);
    }

    public OrderComplaintResponse resolveComplaint(Long complaintId, ResolveOrderComplaintRequest request) {
        OrderComplaint complaint = orderComplaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khiếu nại với id: " + complaintId));

        if (complaint.getDecision() != OrderComplaint.Decision.PENDING) {
            throw new RuntimeException("Khiếu nại này đã được xử lý trước đó");
        }

        OrderComplaint.Decision nextDecision = parseDecision(request.getDecision());
        complaint.setDecision(nextDecision);
        complaint.setResolutionNote(safeTrim(request.getResolutionNote()));
        complaint.setResolvedAt(LocalDateTime.now());
        complaint.setResolvedBy(getCurrentUsername());

        switch (nextDecision) {
            case REFUND_APPROVED -> {
                Double approvedRefundAmount = request.getApprovedRefundAmount();
                if (approvedRefundAmount == null || approvedRefundAmount < 0) {
                    throw new RuntimeException("approvedRefundAmount không hợp lệ cho quyết định hoàn tiền");
                }
                complaint.setApprovedRefundAmount(approvedRefundAmount);
                complaint.getOrder().setStatus(Order.Status.REFUNDED);
            }
            case CANCEL_APPROVED -> {
                complaint.setApprovedRefundAmount(0.0);
                complaint.getOrder().setStatus(Order.Status.CANCELED);
            }
            case REJECTED, PENDING -> complaint.setApprovedRefundAmount(0.0);
            default -> throw new RuntimeException("decision không hợp lệ");
        }

        orderRepository.save(complaint.getOrder());
        OrderComplaint saved = orderComplaintRepository.save(complaint);

        auditLogService.logAction(
                "ADMIN_RESOLVE_ORDER_COMPLAINT",
                "ORDER#" + complaint.getOrder().getId(),
                "Decision=" + nextDecision.name() + " | Complaint#" + complaintId);

        return toComplaintResponse(saved);
    }

    private OrderSummaryResponse toOrderSummary(Order order) {
        List<OrderItemSummaryResponse> itemResponses = order.getOrderItems() == null
                ? List.of()
                : order.getOrderItems().stream()
                        .map(item -> {
                        double unitPrice = safeDouble(item.getPrice());
                        int quantity = safeInt(item.getQuantity());
                            return new OrderItemSummaryResponse(
                                    item.getProduct() == null ? null : item.getProduct().getId(),
                                    item.getProduct() == null ? "N/A" : item.getProduct().getName(),
                                    quantity,
                                    unitPrice,
                                    unitPrice * quantity);
                        })
                        .collect(Collectors.toList());

        int totalItems = itemResponses.stream()
        .mapToInt(item -> safeInt(item.getQuantity()))
                .sum();

        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderDate(),
                safeDouble(order.getTotalAmount()),
                safeStatusName(order.getStatus()),
                order.getUser() == null ? null : order.getUser().getId(),
                resolveCustomerName(order),
                order.getShippingAddress(),
                totalItems,
                itemResponses);
    }

    private OrderComplaintResponse toComplaintResponse(OrderComplaint complaint) {
        return new OrderComplaintResponse(
                complaint.getId(),
                complaint.getOrder().getId(),
                resolveCustomerName(complaint.getOrder()),
                complaint.getReason(),
                complaint.getRequestedRefundAmount(),
                complaint.getDecision().name(),
                complaint.getApprovedRefundAmount(),
                complaint.getResolutionNote(),
                complaint.getRequestedAt(),
                complaint.getResolvedAt(),
                complaint.getResolvedBy());
    }

    private Order.Status parseOrderStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.trim().isEmpty()) {
            throw new RuntimeException("status không được để trống");
        }

        try {
            return Order.Status.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("status không hợp lệ");
        }
    }

    private OrderComplaint.Decision parseDecision(String rawDecision) {
        if (rawDecision == null || rawDecision.trim().isEmpty()) {
            throw new RuntimeException("decision không được để trống");
        }

        try {
            return OrderComplaint.Decision.valueOf(rawDecision.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("decision không hợp lệ");
        }
    }

    private boolean filterByStatus(Order order, String normalizedStatus) {
        if (normalizedStatus.isEmpty()) {
            return true;
        }
        return order.getStatus() != null && order.getStatus().name().equalsIgnoreCase(normalizedStatus);
    }

    private boolean filterByQuery(Order order, String normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            return true;
        }

        String customerName = resolveCustomerName(order);
        return String.valueOf(order.getId()).contains(normalizedQuery)
                || containsIgnoreCase(customerName, normalizedQuery)
                || containsIgnoreCase(order.getShippingAddress(), normalizedQuery);
    }

    private String resolveCustomerName(Order order) {
        if (order == null || order.getUser() == null) {
            return "Khách vãng lai";
        }

        String fullName = safeTrim(order.getUser().getFullName());
        if (!fullName.isEmpty()) {
            return fullName;
        }

        String username = safeTrim(order.getUser().getUsername());
        return username.isEmpty() ? "Khách vãng lai" : username;
    }

    private String normalize(String input) {
        return input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
    }

    private boolean containsIgnoreCase(String source, String query) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(query);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeStatusName(Order.Status status) {
        return status == null ? "UNKNOWN" : status.name();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }

        String principalName = authentication.getName();
        return principalName == null || principalName.isBlank() ? "system" : principalName;
    }
}
