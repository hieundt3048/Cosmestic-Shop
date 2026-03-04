package com.cosmeticshop.cosmetic.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;

@Service
/**
 * Service điều phối thống kê doanh thu theo Strategy Pattern.
 *
 * Vai trò chính:
 * - Nhận tham số range từ controller.
 * - Tìm strategy phù hợp theo range.
 * - Delegate xử lý aggregate cho strategy tương ứng.
 *
 * Lợi ích:
 * - Dễ mở rộng thêm range mới (quarter, custom...) mà không sửa logic hiện tại.
 */
public class RevenueReportService implements IRevenueReportService {

    /**
     * Registry strategy theo key (day/week/month/year).
     * Key được lấy từ RevenueRangeStrategy#getRangeKey().
     */
    private final Map<String, RevenueRangeStrategy> strategyByRange;

    /**
     * Spring inject toàn bộ bean implement RevenueRangeStrategy,
     * sau đó map thành bảng tra cứu O(1) theo key.
     */
    public RevenueReportService(List<RevenueRangeStrategy> strategies) {
        this.strategyByRange = strategies.stream()
                .collect(Collectors.toMap(
                        RevenueRangeStrategy::getRangeKey,
                        Function.identity()));
    }

    /**
     * Trả dữ liệu doanh thu theo range yêu cầu.
     *
     * @param range day | week | month | year (null => month)
     * @return dữ liệu thống kê doanh thu cho biểu đồ
     */
    @Override
    public RevenueStatisticsResponse getRevenueByRange(String range) {
        // Chuẩn hóa input: trim + lower-case để tránh sai do format chuỗi.
        String normalizedRange = range == null ? "month" : range.trim().toLowerCase();

        // Lấy strategy tương ứng từ registry.
        RevenueRangeStrategy strategy = strategyByRange.get(normalizedRange);

        // Fail-fast: báo lỗi rõ ràng nếu range không được hỗ trợ.
        if (strategy == null) {
            throw new RuntimeException("range không hợp lệ. Chỉ chấp nhận: " + String.join(", ", strategyByRange.keySet()));
        }

        // Delegate sang strategy cụ thể, service không chứa business logic aggregate chi tiết.
        return strategy.aggregate();
    }
}
