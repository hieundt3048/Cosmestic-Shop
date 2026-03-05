package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.TopSellingProductResponse;
import com.cosmeticshop.cosmetic.Repository.OrderRepository;

@Service
// Service thống kê sản phẩm bán chạy theo khoảng thời gian
public class TopSellingProductReportService implements ITopSellingProductReportService {

    private final OrderRepository orderRepository;

    public TopSellingProductReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    // Trả danh sách top sản phẩm bán chạy cho dashboard/admin report
    public List<TopSellingProductResponse> getTopSellingProducts(String range, Integer limit) {
        // Chuẩn hóa input và set default
        String normalizedRange = range == null ? "month" : range.trim().toLowerCase();
        int normalizedLimit = limit == null || limit <= 0 ? 5 : limit;

        // Xác định khoảng thời gian báo cáo
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = resolveStartDate(normalizedRange, endDate);

        // Query top sản phẩm đã giao thành công (DELIVERED), có phân trang theo limit
        List<Object[]> rows = orderRepository.findTopSellingProducts(
                startDate,
                endDate,
                PageRequest.of(0, normalizedLimit));

        // Map kết quả thô (Object[]) về DTO typed để FE dễ dùng
        return rows.stream()
                .map(row -> new TopSellingProductResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).intValue(),
                        row[3] == null ? 0.0 : ((Number) row[3]).doubleValue()))
                .toList();
    }

    // Resolve ngày bắt đầu theo loại range
    private LocalDate resolveStartDate(String range, LocalDate endDate) {
        return switch (range) {
            // 7 ngày gần nhất
            case "day" -> endDate.minusDays(6);
            // 8 tuần gần nhất
            case "week" -> endDate.minusWeeks(7);
            // 12 tháng gần nhất
            case "month" -> YearMonth.from(endDate).minusMonths(11).atDay(1);
            // 5 năm gần nhất
            case "year" -> LocalDate.of(endDate.getYear() - 4, 1, 1);
            default -> throw new RuntimeException("range không hợp lệ. Chỉ chấp nhận: day, week, month, year");
        };
    }
}
