package com.cosmeticshop.cosmetic.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateVoucherRequest;
import com.cosmeticshop.cosmetic.Dto.UpdateVoucherStatusRequest;
import com.cosmeticshop.cosmetic.Dto.VoucherResponse;
import com.cosmeticshop.cosmetic.Entity.Product;
import com.cosmeticshop.cosmetic.Entity.Voucher;
import com.cosmeticshop.cosmetic.Exception.ResourceNotFoundException;
import com.cosmeticshop.cosmetic.Repository.ProductRepository;
import com.cosmeticshop.cosmetic.Repository.VoucherRepository;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final ProductRepository productRepository;

    public VoucherService(VoucherRepository voucherRepository, ProductRepository productRepository) {
        this.voucherRepository = voucherRepository;
        this.productRepository = productRepository;
    }

    public List<VoucherResponse> getAllVouchers() {
        return voucherRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toVoucherResponse)
                .collect(Collectors.toList());
    }

    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        String normalizedCode = normalizeCode(request.getCode());
        Voucher.Scope scope = parseScope(request.getScope());

        if (voucherRepository.findByCodeIgnoreCase(normalizedCode).isPresent()) {
            throw new RuntimeException("Voucher code đã tồn tại: " + normalizedCode);
        }

        validateDiscount(request.getDiscountPercent());

        Voucher voucher = new Voucher();
        voucher.setCode(normalizedCode);
        voucher.setDiscountPercent(request.getDiscountPercent());
        voucher.setScope(scope);
        voucher.setActive(request.getActive() == null || request.getActive());
        voucher.setProduct(resolveProductByScope(scope, request.getProductId()));

        return toVoucherResponse(voucherRepository.save(voucher));
    }

    public VoucherResponse updateVoucherStatus(Long id, UpdateVoucherStatusRequest request) {
        if (request.getActive() == null) {
            throw new RuntimeException("active không được để trống");
        }

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với id: " + id));

        voucher.setActive(request.getActive());
        return toVoucherResponse(voucherRepository.save(voucher));
    }

    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với id: " + id));
        voucherRepository.delete(voucher);
    }

    private String normalizeCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("Voucher code không được để trống");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private Voucher.Scope parseScope(String scope) {
        if (scope == null || scope.trim().isEmpty()) {
            return Voucher.Scope.STORE;
        }

        try {
            return Voucher.Scope.valueOf(scope.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("scope không hợp lệ. Chỉ chấp nhận STORE hoặc PRODUCT");
        }
    }

    private Product resolveProductByScope(Voucher.Scope scope, Long productId) {
        if (scope == Voucher.Scope.PRODUCT) {
            if (productId == null) {
                throw new RuntimeException("productId là bắt buộc khi scope = PRODUCT");
            }

            return productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với id: " + productId));
        }

        return null;
    }

    private void validateDiscount(Double discountPercent) {
        if (discountPercent == null || discountPercent <= 0 || discountPercent > 100) {
            throw new RuntimeException("discountPercent phải nằm trong khoảng (0, 100]");
        }
    }

    private VoucherResponse toVoucherResponse(Voucher voucher) {
        VoucherResponse.ProductSummary productSummary = null;
        if (voucher.getProduct() != null) {
            productSummary = new VoucherResponse.ProductSummary(
                    voucher.getProduct().getId(),
                    voucher.getProduct().getName());
        }

        return new VoucherResponse(
                voucher.getId(),
                voucher.getCode(),
                voucher.getDiscountPercent(),
                voucher.getScope().name(),
                voucher.isActive(),
                productSummary,
                voucher.getCreatedAt(),
                voucher.getUpdatedAt());
    }
}
