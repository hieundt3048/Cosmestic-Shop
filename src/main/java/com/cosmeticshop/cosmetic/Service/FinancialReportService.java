package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.FinancialReportPointDto;
import com.cosmeticshop.cosmetic.Dto.FinancialReportResponse;
import com.cosmeticshop.cosmetic.Entity.Order;
import com.cosmeticshop.cosmetic.Repository.OrderRepository;

@Service
public class FinancialReportService {

    // Tạm tính theo tỷ lệ để có thể theo dõi xu hướng tài chính khi chưa có bảng chi phí chi tiết.
    private static final double TAX_RATE = 0.10;
    private static final double SHIPPING_RATE = 0.06;

    private final OrderRepository orderRepository;

    public FinancialReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public FinancialReportResponse getFinancialReport(String range) {
        String normalizedRange = range == null ? "month" : range.trim().toLowerCase(Locale.ROOT);
        LocalDate today = LocalDate.now();

        RangeWindow window = switch (normalizedRange) {
            case "day" -> {
                LocalDate startDate = today.minusDays(6);
                LocalDate endDate = today;
                yield new RangeWindow(startDate, endDate, initDailyBucket(startDate, endDate));
            }
            case "week" -> {
                LocalDate startDate = today.minusWeeks(7);
                LocalDate endDate = today;
                yield new RangeWindow(startDate, endDate, initWeeklyBucket(startDate, endDate));
            }
            case "year" -> {
                LocalDate startDate = LocalDate.of(today.getYear() - 4, 1, 1);
                LocalDate endDate = LocalDate.of(today.getYear(), 12, 31);
                yield new RangeWindow(startDate, endDate, initYearlyBucket(startDate, endDate));
            }
            case "month" -> {
                LocalDate startDate = today.minusMonths(11).withDayOfMonth(1);
                LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());
                yield new RangeWindow(startDate, endDate, initMonthlyBucket(startDate, endDate));
            }
            default -> throw new RuntimeException("range không hợp lệ. Chỉ chấp nhận: day, week, month, year");
        };

        LocalDate startDate = window.startDate;
        LocalDate endDate = window.endDate;
        Map<String, Double> revenueBucket = window.revenueBucket;

        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        for (Order order : orders) {
            if (order.getOrderDate() == null || !isFinanciallyValid(order.getStatus())) {
                continue;
            }

            String key = resolveKey(normalizedRange, order.getOrderDate());
            if (key == null) {
                continue;
            }

            revenueBucket.computeIfPresent(key, (ignored, value) -> value + safeAmount(order.getTotalAmount()));
        }

        List<FinancialReportPointDto> points = revenueBucket.entrySet().stream()
                .map(entry -> {
                    double revenue = entry.getValue();
                    double shipping = revenue * SHIPPING_RATE;
                    double tax = revenue * TAX_RATE;
                    double netProfit = revenue - shipping - tax;
                    return new FinancialReportPointDto(entry.getKey(), revenue, shipping, tax, netProfit);
                })
                .collect(Collectors.toList());

        double totalRevenue = points.stream().mapToDouble(item -> safeAmount(item.getRevenue())).sum();
        double totalShipping = points.stream().mapToDouble(item -> safeAmount(item.getShipping())).sum();
        double totalTax = points.stream().mapToDouble(item -> safeAmount(item.getTax())).sum();
        double totalNetProfit = points.stream().mapToDouble(item -> safeAmount(item.getNetProfit())).sum();

        return new FinancialReportResponse(
                normalizedRange,
                points,
                totalRevenue,
                totalShipping,
                totalTax,
                totalNetProfit);
    }

    private boolean isFinanciallyValid(Order.Status status) {
        return status != null && status != Order.Status.CANCELED && status != Order.Status.REFUNDED;
    }

    private String resolveKey(String range, LocalDate date) {
        return switch (range) {
            case "day" -> String.format("%02d/%02d", date.getDayOfMonth(), date.getMonthValue());
            case "week" -> {
                WeekFields weekFields = WeekFields.ISO;
                int week = date.get(weekFields.weekOfWeekBasedYear());
                int year = date.get(weekFields.weekBasedYear());
                yield "W" + week + "/" + year;
            }
            case "month" -> String.format("%02d/%d", date.getMonthValue(), date.getYear());
            case "year" -> String.valueOf(date.getYear());
            default -> null;
        };
    }

    private Map<String, Double> initDailyBucket(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> bucket = new LinkedHashMap<>();
        for (LocalDate cursor = startDate; !cursor.isAfter(endDate); cursor = cursor.plusDays(1)) {
            bucket.put(resolveKey("day", cursor), 0.0);
        }
        return bucket;
    }

    private Map<String, Double> initWeeklyBucket(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> bucket = new LinkedHashMap<>();
        for (LocalDate cursor = startDate; !cursor.isAfter(endDate); cursor = cursor.plusWeeks(1)) {
            bucket.put(resolveKey("week", cursor), 0.0);
        }
        return bucket;
    }

    private Map<String, Double> initMonthlyBucket(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> bucket = new LinkedHashMap<>();
        for (LocalDate cursor = startDate; !cursor.isAfter(endDate); cursor = cursor.plusMonths(1)) {
            bucket.put(resolveKey("month", cursor), 0.0);
        }
        return bucket;
    }

    private Map<String, Double> initYearlyBucket(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> bucket = new LinkedHashMap<>();
        for (LocalDate cursor = startDate; !cursor.isAfter(endDate); cursor = cursor.plusYears(1)) {
            bucket.put(resolveKey("year", cursor), 0.0);
        }
        return bucket;
    }

    private double safeAmount(Double amount) {
        return amount == null ? 0.0 : amount;
    }

    private static final class RangeWindow {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Map<String, Double> revenueBucket;

        private RangeWindow(LocalDate startDate, LocalDate endDate, Map<String, Double> revenueBucket) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.revenueBucket = revenueBucket;
        }
    }
}
