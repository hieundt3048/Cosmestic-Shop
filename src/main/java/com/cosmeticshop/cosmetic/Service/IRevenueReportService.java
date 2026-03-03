package com.cosmeticshop.cosmetic.Service;

import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;

public interface IRevenueReportService {
    RevenueStatisticsResponse getRevenueByRange(String range);
}
