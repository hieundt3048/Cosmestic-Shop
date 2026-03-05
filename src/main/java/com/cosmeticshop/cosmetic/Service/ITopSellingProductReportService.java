package com.cosmeticshop.cosmetic.Service;

import java.util.List;

import com.cosmeticshop.cosmetic.Dto.TopSellingProductResponse;

public interface ITopSellingProductReportService {
    List<TopSellingProductResponse> getTopSellingProducts(String range, Integer limit);
}
