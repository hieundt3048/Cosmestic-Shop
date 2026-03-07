package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.CreateVoucherRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateVoucherStatusRequest;
import com.cosmeticshop.cosmetic.Dto.VoucherResponse;
import com.cosmeticshop.cosmetic.Service.VoucherService;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<VoucherResponse> getAllVouchers() {
        return voucherService.getAllVouchers();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public VoucherResponse createVoucher(@RequestBody CreateVoucherRequest request) {
        return voucherService.createVoucher(request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public VoucherResponse updateVoucherStatus(
            @PathVariable Long id,
            @RequestBody UpdateVoucherStatusRequest request) {
        return voucherService.updateVoucherStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
    }
}
