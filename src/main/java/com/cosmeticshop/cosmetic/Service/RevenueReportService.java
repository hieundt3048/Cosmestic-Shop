package com.cosmeticshop.cosmetic.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;

@Service
public class RevenueReportService implements IRevenueReportService {

    private final Map<String, RevenueRangeStrategy> strategyByRange;

    public RevenueReportService(List<RevenueRangeStrategy> strategies) {
        this.strategyByRange = strategies.stream()
                .collect(Collectors.toMap(
                        RevenueRangeStrategy::getRangeKey,
                        Function.identity()));
    }

    @Override
    public RevenueStatisticsResponse getRevenueByRange(String range) {
        String normalizedRange = range == null ? "month" : range.trim().toLowerCase();
        RevenueRangeStrategy strategy = strategyByRange.get(normalizedRange);

        if (strategy == null) {
            throw new RuntimeException("range không hợp lệ. Chỉ chấp nhận: " + String.join(", ", strategyByRange.keySet()));
        }

        return strategy.aggregate();
    }
}
