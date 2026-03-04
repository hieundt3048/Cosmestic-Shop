package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.cosmeticshop.cosmetic.Dto.RevenuePointDto;
import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;
import com.cosmeticshop.cosmetic.Entity.Order;
import com.cosmeticshop.cosmetic.Repository.OrderRepository;

// Base class theo Template Method cho các strategy doanh thu (day/week/month/year)
public abstract class BaseRevenueRangeStrategy implements RevenueRangeStrategy {

    // Repository dùng chung để lấy đơn hàng trong khoảng thời gian
    protected final OrderRepository orderRepository;

    protected BaseRevenueRangeStrategy(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Luồng xử lý dùng chung:
    // 1) Query orders theo khoảng thời gian
    // 2) Loại đơn không hợp lệ (null date / canceled)
    // 3) Resolve key theo strategy con và cộng dồn doanh thu vào bucket
    // 4) Convert bucket -> response DTO
    protected RevenueStatisticsResponse buildStatistics(
            String range,
            Map<String, Double> bucket,
            LocalDate startDate,
            LocalDate endDate,
            Function<Order, String> keyResolver) {

        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        for (Order order : orders) {
            if (order.getOrderDate() == null || order.getStatus() == Order.Status.CANCELED) {
                continue;
            }

            // keyResolver do strategy con cung cấp (vd: dd/MM, W12/2026, MM/yyyy...)
            String key = keyResolver.apply(order);
            if (key == null) {
                continue;
            }

            // Chỉ cộng dồn cho key tồn tại sẵn trong bucket đã khởi tạo
            bucket.computeIfPresent(key, (ignored, value) -> value + safeAmount(order.getTotalAmount()));
        }

        return toResponse(range, bucket);
    }

    // Chuyển map doanh thu thành danh sách point + tổng doanh thu toàn kỳ
    private RevenueStatisticsResponse toResponse(String range, Map<String, Double> bucket) {
        List<RevenuePointDto> points = new ArrayList<>();
        double totalRevenue = 0.0;

        for (Map.Entry<String, Double> entry : bucket.entrySet()) {
            points.add(new RevenuePointDto(entry.getKey(), entry.getValue()));
            totalRevenue += entry.getValue();
        }

        return new RevenueStatisticsResponse(range, points, totalRevenue);
    }

    // Null-safe amount để tránh NPE khi dữ liệu thiếu totalAmount
    private double safeAmount(Double amount) {
        return amount == null ? 0.0 : amount;
    }
}
