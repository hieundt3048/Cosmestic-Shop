package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;
import com.cosmeticshop.cosmetic.Repository.OrderRepository;

@Component
/**
 * Revenue strategy cho che do "week":
 * - Tong hop doanh thu theo tuan
 * - Cua so 8 tuan gan nhat
 */
public class WeeklyRevenueRangeStrategy extends BaseRevenueRangeStrategy {

    public WeeklyRevenueRangeStrategy(OrderRepository orderRepository) {
        super(orderRepository);
    }

    @Override
    public String getRangeKey() {
        return "week";
    }

    @Override
    public RevenueStatisticsResponse aggregate() {
        // Lay khoang 8 tuan lien tiep tinh den hien tai.
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(7);
        // Dung WeekFields theo locale de tinh so tuan dung theo vung.
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        // Khoi tao bucket co thu tu on dinh: W<week>/<year> de FE ve chart.
        Map<String, Double> bucket = new LinkedHashMap<>();
        for (int index = 0; index < 8; index++) {
            LocalDate date = startDate.plusWeeks(index);
            int week = date.get(weekFields.weekOfWeekBasedYear());
            int year = date.get(weekFields.weekBasedYear());
            bucket.put("W" + week + "/" + year, 0.0);
        }

        return buildStatistics(
                getRangeKey(),
                bucket,
                startDate,
                endDate,
                order -> {
                    // Group key cua tung order theo week-based year.
                    int week = order.getOrderDate().get(weekFields.weekOfWeekBasedYear());
                    int year = order.getOrderDate().get(weekFields.weekBasedYear());
                    return "W" + week + "/" + year;
                });
    }
}
