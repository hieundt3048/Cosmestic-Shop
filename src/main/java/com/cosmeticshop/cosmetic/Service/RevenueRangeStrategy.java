package com.cosmeticshop.cosmetic.Service;

import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;

public interface RevenueRangeStrategy {

    String getRangeKey();

    RevenueStatisticsResponse aggregate();
}
