package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;
import com.cosmeticshop.cosmetic.Repository.OrderRepository;

@Component
public class MonthlyRevenueRangeStrategy extends BaseRevenueRangeStrategy {

    public MonthlyRevenueRangeStrategy(OrderRepository orderRepository) {
        super(orderRepository);
    }

    @Override
    public String getRangeKey() {
        return "month";
    }

    @Override
    public RevenueStatisticsResponse aggregate() {
        LocalDate endDate = LocalDate.now();
        YearMonth endMonth = YearMonth.from(endDate);
        YearMonth startMonth = endMonth.minusMonths(11);
        LocalDate startDate = startMonth.atDay(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        Map<String, Double> bucket = new LinkedHashMap<>();
        for (int index = 0; index < 12; index++) {
            YearMonth month = startMonth.plusMonths(index);
            bucket.put(month.format(formatter), 0.0);
        }

        return buildStatistics(
                getRangeKey(),
                bucket,
                startDate,
                endDate,
                order -> YearMonth.from(order.getOrderDate()).format(formatter));
    }
}
