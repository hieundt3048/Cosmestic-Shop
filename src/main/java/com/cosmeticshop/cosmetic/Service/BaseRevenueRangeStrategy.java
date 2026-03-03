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

public abstract class BaseRevenueRangeStrategy implements RevenueRangeStrategy {

    protected final OrderRepository orderRepository;

    protected BaseRevenueRangeStrategy(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

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

            String key = keyResolver.apply(order);
            if (key == null) {
                continue;
            }

            bucket.computeIfPresent(key, (ignored, value) -> value + safeAmount(order.getTotalAmount()));
        }

        return toResponse(range, bucket);
    }

    private RevenueStatisticsResponse toResponse(String range, Map<String, Double> bucket) {
        List<RevenuePointDto> points = new ArrayList<>();
        double totalRevenue = 0.0;

        for (Map.Entry<String, Double> entry : bucket.entrySet()) {
            points.add(new RevenuePointDto(entry.getKey(), entry.getValue()));
            totalRevenue += entry.getValue();
        }

        return new RevenueStatisticsResponse(range, points, totalRevenue);
    }

    private double safeAmount(Double amount) {
        return amount == null ? 0.0 : amount;
    }
}
