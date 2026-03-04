package com.cosmeticshop.cosmetic.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.OrderKpiResponse;
import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;
import com.cosmeticshop.cosmetic.Service.IOrderKpiReportService;
import com.cosmeticshop.cosmetic.Service.IRevenueReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final IRevenueReportService revenueReportService;
    private final IOrderKpiReportService orderKpiReportService;

    public ReportController(
            IRevenueReportService revenueReportService,
            IOrderKpiReportService orderKpiReportService) {
        this.revenueReportService = revenueReportService;
        this.orderKpiReportService = orderKpiReportService;
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
}
