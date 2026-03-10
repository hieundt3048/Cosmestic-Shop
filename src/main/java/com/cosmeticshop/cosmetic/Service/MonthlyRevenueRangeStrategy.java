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
/**
 * Revenue strategy cho che do "month":
 * - Tong hop doanh thu theo thang
 * - Cua so 12 thang gan nhat
 */
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
        // Khoang thoi gian 12 thang: tu dau thang cua 11 thang truoc den ngay hien tai.
        LocalDate endDate = LocalDate.now();
        YearMonth endMonth = YearMonth.from(endDate);
        YearMonth startMonth = endMonth.minusMonths(11);
        LocalDate startDate = startMonth.atDay(1);
        // Dinh dang label bucket cho FE: MM/yyyy.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        // Khoi tao bucket co thu tu on dinh de FE ve chart dung timeline.
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
            // Group key cua tung order theo thang nam cung format MM/yyyy.
                order -> YearMonth.from(order.getOrderDate()).format(formatter));
    }
}
