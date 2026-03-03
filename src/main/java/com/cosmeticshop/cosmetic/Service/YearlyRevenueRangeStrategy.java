package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;
import com.cosmeticshop.cosmetic.Repository.OrderRepository;

@Component
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
        LocalDate endDate = LocalDate.now();
        int endYear = endDate.getYear();
        int startYear = endYear - 4;
        LocalDate startDate = LocalDate.of(startYear, 1, 1);

        Map<String, Double> bucket = new LinkedHashMap<>();
        for (int year = startYear; year <= endYear; year++) {
            bucket.put(String.valueOf(year), 0.0);
        }

        return buildStatistics(
                getRangeKey(),
                bucket,
                startDate,
                endDate,
                order -> String.valueOf(order.getOrderDate().getYear()));
    }
}
