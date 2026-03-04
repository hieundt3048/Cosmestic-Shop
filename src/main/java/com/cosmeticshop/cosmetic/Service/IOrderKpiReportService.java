package com.cosmeticshop.cosmetic.Service;

import com.cosmeticshop.cosmetic.Dto.OrderKpiResponse;

public interface IOrderKpiReportService {
    OrderKpiResponse getOrderKpisByRange(String range);
}
