package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;
import com.cosmeticshop.cosmetic.Repository.OrderRepository;

@Component
/**
 * Revenue strategy cho che do "year":
 * - Tong hop doanh thu theo nam
 * - Cua so 5 nam gan nhat (nam hien tai va 4 nam truoc)
 */
public class YearlyRevenueRangeStrategy extends BaseRevenueRangeStrategy {

    public YearlyRevenueRangeStrategy(OrderRepository orderRepository) {
        super(orderRepository);
    }

    @Override
    public String getRangeKey() {
        return "year";
    }

    @Override
    public RevenueStatisticsResponse aggregate() {
        // Xac dinh khoang thoi gian 5 nam gan nhat.
        LocalDate endDate = LocalDate.now();
        int endYear = endDate.getYear();
        int startYear = endYear - 4;
        LocalDate startDate = LocalDate.of(startYear, 1, 1);

        // Khoi tao bucket co thu tu on dinh de FE ve chart dung truc nam.
        Map<String, Double> bucket = new LinkedHashMap<>();
        for (int year = startYear; year <= endYear; year++) {
            bucket.put(String.valueOf(year), 0.0);
        }

        // Group key theo nam cua orderDate, vi du: "2026".
        return buildStatistics(
                getRangeKey(),
                bucket,
                startDate,
                endDate,
                order -> String.valueOf(order.getOrderDate().getYear()));
    }
}
