package com.cosmeticshop.cosmetic.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.RevenueStatisticsResponse;
import com.cosmeticshop.cosmetic.Service.IRevenueReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final IRevenueReportService revenueReportService;

    public ReportController(IRevenueReportService revenueReportService) {
        this.revenueReportService = revenueReportService;
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RevenueStatisticsResponse> getRevenueByRange(
            @RequestParam(defaultValue = "month") String range) {
        return ResponseEntity.ok(revenueReportService.getRevenueByRange(range));
    }
}
