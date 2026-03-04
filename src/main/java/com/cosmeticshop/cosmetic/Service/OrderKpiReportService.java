package com.cosmeticshop.cosmetic.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.OrderKpiResponse;
import com.cosmeticshop.cosmetic.Entity.Order;
import com.cosmeticshop.cosmetic.Repository.OrderRepository;

@Service
// Service tổng hợp KPI đơn hàng cho dashboard admin
public class OrderKpiReportService implements IOrderKpiReportService {

    private final OrderRepository orderRepository;

    public OrderKpiReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    // Tính KPI theo khoảng thời gian: day/week/month/year
    public OrderKpiResponse getOrderKpisByRange(String range) {
        // Chuẩn hóa input và default về month nếu client không truyền range
        String normalizedRange = range == null ? "month" : range.trim().toLowerCase();

        // endDate là ngày hiện tại, startDate phụ thuộc vào loại range
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = resolveStartDate(normalizedRange, endDate);

        // Lấy toàn bộ đơn hàng trong khoảng [startDate, endDate]
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);

        // Tổng số đơn trong kỳ (mọi trạng thái)
        int totalOrders = orders.size();

        // Đơn thành công: quy ước là trạng thái DELIVERED
        int successfulOrders = (int) orders.stream()
                .filter(order -> order.getStatus() == Order.Status.DELIVERED)
                .count();

        // Đơn hủy: trạng thái CANCELED
        int canceledOrders = (int) orders.stream()
                .filter(order -> order.getStatus() == Order.Status.CANCELED)
                .count();

        // Tỷ lệ chuyển đổi = successfulOrders / totalOrders * 100
        // Dùng BigDecimal để làm tròn 2 chữ số thập phân (HALF_UP)
        // Nếu không có đơn hàng thì conversionRate = 0 để tránh chia cho 0
        double conversionRate = totalOrders == 0
                ? 0.0
                : BigDecimal.valueOf((successfulOrders * 100.0) / totalOrders)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

        // Trả DTO gọn cho frontend dashboard
        return new OrderKpiResponse(
                normalizedRange,
                totalOrders,
                successfulOrders,
                canceledOrders,
                conversionRate);
    }

    // Xác định ngày bắt đầu cho từng loại range
    private LocalDate resolveStartDate(String range, LocalDate endDate) {
        return switch (range) {
            // 7 ngày gần nhất
            case "day" -> endDate.minusDays(6);

            // 8 tuần gần nhất
            case "week" -> endDate.minusWeeks(7);

            // 12 tháng gần nhất (từ ngày đầu tháng)
            case "month" -> YearMonth.from(endDate).minusMonths(11).atDay(1);

            // 5 năm gần nhất (từ 01/01 của năm bắt đầu)
            case "year" -> LocalDate.of(endDate.getYear() - 4, 1, 1);
            default -> throw new RuntimeException("range không hợp lệ. Chỉ chấp nhận: day, week, month, year");
        };
    }
}
