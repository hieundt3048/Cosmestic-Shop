package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;
import com.cosmeticshop.cosmetic.Repository.OrderRepository;

@Component
public class DailyRevenueRangeStrategy extends BaseRevenueRangeStrategy {

    public DailyRevenueRangeStrategy(OrderRepository orderRepository) {
        super(orderRepository);
    }

    @Override
    public String getRangeKey() {
        return "day";
    }

    @Override
    public RevenueStatisticsResponse aggregate() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        Map<String, Double> bucket = new LinkedHashMap<>();
        for (int index = 0; index < 7; index++) {
            LocalDate date = startDate.plusDays(index);
            bucket.put(date.format(formatter), 0.0);
        }

        return buildStatistics(getRangeKey(), bucket, startDate, endDate, order -> order.getOrderDate().format(formatter));
    }
}
