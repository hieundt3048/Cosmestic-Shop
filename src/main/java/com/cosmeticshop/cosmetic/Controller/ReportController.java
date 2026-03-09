package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.ActiveUserTrafficResponse;
import com.cosmeticshop.cosmetic.Dto.FinancialReportResponse;
import com.cosmeticshop.cosmetic.Dto.OrderKpiResponse;
import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;
import com.cosmeticshop.cosmetic.Dto.TopSellingProductResponse;
import com.cosmeticshop.cosmetic.Service.FinancialReportService;
import com.cosmeticshop.cosmetic.Service.IActiveUserTrackingService;
import com.cosmeticshop.cosmetic.Service.IOrderKpiReportService;
import com.cosmeticshop.cosmetic.Service.IRevenueReportService;
import com.cosmeticshop.cosmetic.Service.ITopSellingProductReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final IRevenueReportService revenueReportService;
    private final IOrderKpiReportService orderKpiReportService;
    private final ITopSellingProductReportService topSellingProductReportService;
    private final IActiveUserTrackingService activeUserTrackingService;
    private final FinancialReportService financialReportService;

    public ReportController(
            IRevenueReportService revenueReportService,
            IOrderKpiReportService orderKpiReportService,
            ITopSellingProductReportService topSellingProductReportService,
            IActiveUserTrackingService activeUserTrackingService,
            FinancialReportService financialReportService) {
        this.revenueReportService = revenueReportService;
        this.orderKpiReportService = orderKpiReportService;
        this.topSellingProductReportService = topSellingProductReportService;
        this.activeUserTrackingService = activeUserTrackingService;
        this.financialReportService = financialReportService;
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RevenueStatisticsResponse> getRevenueByRange(
            @RequestParam(defaultValue = "month") String range) {
        return ResponseEntity.ok(revenueReportService.getRevenueByRange(range));
    }

    @GetMapping("/order-kpis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderKpiResponse> getOrderKpisByRange(
            @RequestParam(defaultValue = "month") String range) {
        return ResponseEntity.ok(orderKpiReportService.getOrderKpisByRange(range));
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopSellingProductResponse>> getTopSellingProducts(
            @RequestParam(defaultValue = "month") String range,
            @RequestParam(defaultValue = "5") Integer limit) {
        return ResponseEntity.ok(topSellingProductReportService.getTopSellingProducts(range, limit));
    }

    @GetMapping("/active-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActiveUserTrafficResponse> getActiveUserTraffic() {
        return ResponseEntity.ok(activeUserTrackingService.getActiveUserTraffic());
    }

    @GetMapping("/financial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialReportResponse> getFinancialReport(
            @RequestParam(defaultValue = "month") String range) {
        return ResponseEntity.ok(financialReportService.getFinancialReport(range));
    }
}
