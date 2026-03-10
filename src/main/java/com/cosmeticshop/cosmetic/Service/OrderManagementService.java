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
/**
 * Nghiep vu quan ly don hang cho admin va employee:
 * - Admin: giam sat toan bo don, xu ly khiu nai/hoan tien
 * - Employee: tiep nhan don moi, chuyen trang thai theo quy trinh van hanh, huy don co ly do
 * - Tat ca thao tac quan trong deu ghi audit log
 */
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
        // Chuan hoa input de filter/search on dinh, khong phan biet hoa thuong.
        String normalizedStatus = normalize(status);
        String normalizedQuery = normalize(query);

        return orderRepository.findAll().stream()
                .filter(order -> filterByStatus(order, normalizedStatus))
                .filter(order -> filterByQuery(order, normalizedQuery))
                // Hien thi don moi nhat truoc tren dashboard.
                .sorted((a, b) -> {
                    LocalDateTime left = a.getOrderDate() == null ? LocalDateTime.MIN : a.getOrderDate().atStartOfDay();
                    LocalDateTime right = b.getOrderDate() == null ? LocalDateTime.MIN : b.getOrderDate().atStartOfDay();
                    return right.compareTo(left);
                })
                .map(this::toOrderSummary)
                .collect(Collectors.toList());
    }

    public List<OrderSummaryResponse> getEmployeeOrders(String status, String query) {
        // Employee su dung cung nguon du lieu va bo loc nhu admin de dam bao dong nhat.
        return getAllOrders(status, query);
    }

    public OrderSummaryResponse advanceEmployeeOrderStatus(Long orderId) {
        // Chuyen don sang buoc tiep theo trong quy trinh fulfillment.
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với id: " + orderId));

        Order.Status oldStatus = order.getStatus();
        Order.Status nextStatus = determineNextEmployeeStatus(oldStatus);
        order.setStatus(nextStatus);

        Order saved = orderRepository.save(order);
        auditLogService.logAction(
                "EMPLOYEE_ADVANCE_ORDER_STATUS",
                "ORDER#" + orderId,
                "Status: " + safeStatusName(oldStatus) + " -> " + safeStatusName(nextStatus));

        return toOrderSummary(saved);
    }

    public OrderSummaryResponse cancelOrderByEmployee(Long orderId, String reason) {
        // Huy don tu nghiep vu van hanh (khach yeu cau huy/khong lien lac duoc...).
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Lý do hủy đơn không được để trống");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với id: " + orderId));

        Order.Status currentStatus = order.getStatus();
        if (currentStatus == Order.Status.DELIVERED || currentStatus == Order.Status.REFUNDED) {
            throw new RuntimeException("Không thể hủy đơn đã hoàn tất hoặc đã hoàn tiền");
        }

        if (currentStatus == Order.Status.CANCELED) {
            throw new RuntimeException("Đơn hàng này đã ở trạng thái hủy");
        }

        order.setStatus(Order.Status.CANCELED);
        Order saved = orderRepository.save(order);
        auditLogService.logAction(
                "EMPLOYEE_CANCEL_ORDER",
                "ORDER#" + orderId,
                "Hủy đơn bởi nhân viên. Reason=" + reason.trim());

        return toOrderSummary(saved);
    }

    public OrderSummaryResponse updateOrderStatus(Long orderId, String rawStatus, String reason) {
        // Cap nhat trang thai theo quyet dinh admin va luu vet qua audit.
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
        // Ho tro loc theo decision va tim theo ma don / ten KH / ly do.
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
        // Bat buoc co ly do de dam bao ho so khiu nai day du.
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
        // Moi khiu nai chi duoc xu ly mot lan, tranh ghi de quyet dinh.
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
                // Duyet hoan tien: can so tien hop le va chuyen don sang REFUNDED.
                Double approvedRefundAmount = request.getApprovedRefundAmount();
                if (approvedRefundAmount == null || approvedRefundAmount < 0) {
                    throw new RuntimeException("approvedRefundAmount không hợp lệ cho quyết định hoàn tiền");
                }
                complaint.setApprovedRefundAmount(approvedRefundAmount);
                complaint.getOrder().setStatus(Order.Status.REFUNDED);
            }
            case CANCEL_APPROVED -> {
                // Duyet huy don: dong y huy va dat so tien hoan = 0.
                complaint.setApprovedRefundAmount(0.0);
                complaint.getOrder().setStatus(Order.Status.CANCELED);
            }
            case REJECTED, PENDING -> complaint.setApprovedRefundAmount(0.0);
            default -> throw new RuntimeException("decision không hợp lệ");
        }

        // Luu don truoc, sau do luu khiu nai de trang thai don luon dong bo.
        orderRepository.save(complaint.getOrder());
        OrderComplaint saved = orderComplaintRepository.save(complaint);

        auditLogService.logAction(
                "ADMIN_RESOLVE_ORDER_COMPLAINT",
                "ORDER#" + complaint.getOrder().getId(),
                "Decision=" + nextDecision.name() + " | Complaint#" + complaintId);

        return toComplaintResponse(saved);
    }

    private OrderSummaryResponse toOrderSummary(Order order) {
        // Map item-level data va tinh tong so luong de FE khong can tinh lai.
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
                resolveCustomerPhone(order),
                order.getShippingAddress(),
                totalItems,
                itemResponses);
    }

    private OrderComplaintResponse toComplaintResponse(OrderComplaint complaint) {
        // DTO hoa de tra response gon va on dinh cho FE.
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
        // Parse enum an toan va tra loi thong diep nghiep vu de FE hien thi.
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
        // Parse decision tu payload va chan gia tri ngoai danh muc.
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
        // Uu tien fullName, fallback username, cuoi cung la khach vang lai.
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

    private String resolveCustomerPhone(Order order) {
        // Tra ve phone de employee lien he khach khi xac nhan/huy don.
        if (order == null || order.getUser() == null) {
            return "N/A";
        }

        String phone = safeTrim(order.getUser().getPhone());
        return phone.isEmpty() ? "N/A" : phone;
    }

    private Order.Status determineNextEmployeeStatus(Order.Status currentStatus) {
        // Quy trinh nghiep vu employee: Pending -> Confirmed -> Packing -> Shipped -> Delivered.
        if (currentStatus == null) {
            throw new RuntimeException("Đơn hàng không có trạng thái hợp lệ");
        }

        return switch (currentStatus) {
            case PENDING -> Order.Status.CONFIRMED;
            case CONFIRMED -> Order.Status.PACKING;
            case PACKING -> Order.Status.SHIPPED;
            case SHIPPED -> Order.Status.DELIVERED;
            case DELIVERED, CANCELED, REFUNDED -> throw new RuntimeException("Đơn hàng đã ở trạng thái kết thúc, không thể chuyển bước");
        };
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
