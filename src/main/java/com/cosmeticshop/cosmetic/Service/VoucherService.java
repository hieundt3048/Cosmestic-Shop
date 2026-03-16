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
/**
 * Quan ly nghiep vu voucher:
 * - Tao voucher toan cua hang (STORE) hoac theo san pham (PRODUCT)
 * - Bat/tat trang thai voucher
 * - Xoa voucher
 */
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public VoucherService(
            VoucherRepository voucherRepository,
            ProductRepository productRepository,
            NotificationService notificationService) {
        this.voucherRepository = voucherRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    public List<VoucherResponse> getAllVouchers() {
        // Hien thi voucher moi nhat truoc de admin de theo doi.
        return voucherRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toVoucherResponse)
                .collect(Collectors.toList());
    }

    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        // Chuan hoa code + parse scope ngay tu dau de xu ly thong nhat.
        String normalizedCode = normalizeCode(request.getCode());
        Voucher.Scope scope = parseScope(request.getScope());

        // Chan trung ma voucher (khong phan biet hoa thuong).
        if (voucherRepository.findByCodeIgnoreCase(normalizedCode).isPresent()) {
            throw new RuntimeException("Voucher code đã tồn tại: " + normalizedCode);
        }

        // Chiet khau phai nam trong khoang hop le (0, 100].
        validateDiscount(request.getDiscountPercent());

        Voucher voucher = new Voucher();
        voucher.setCode(normalizedCode);
        voucher.setDiscountPercent(request.getDiscountPercent());
        voucher.setScope(scope);
        voucher.setActive(request.getActive() == null || request.getActive());
        // Neu scope=PRODUCT thi bat buoc resolve product, STORE thi de null.
        voucher.setProduct(resolveProductByScope(scope, request.getProductId()));

        Voucher savedVoucher = voucherRepository.save(voucher);

        if (savedVoucher.isActive()) {
            String title = "Khuyến mãi mới: " + savedVoucher.getCode();
            String content = "Giảm " + savedVoucher.getDiscountPercent() + "% cho "
                    + (savedVoucher.getScope() == Voucher.Scope.STORE ? "toàn bộ cửa hàng" : "sản phẩm chọn lọc")
                    + ".";
            notificationService.broadcastPromotionNotification(title, content, "VOUCHER#" + savedVoucher.getId());
        }

        return toVoucherResponse(savedVoucher);
    }

    public VoucherResponse updateVoucherStatus(Long id, UpdateVoucherStatusRequest request) {
        // API nay chi cho phep doi trang thai active/inactive.
        if (request.getActive() == null) {
            throw new RuntimeException("active không được để trống");
        }

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với id: " + id));

        voucher.setActive(request.getActive());
        return toVoucherResponse(voucherRepository.save(voucher));
    }

    public void deleteVoucher(Long id) {
        // Kiem tra ton tai de tra loi nghiep vu ro rang truoc khi xoa.
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với id: " + id));
        voucherRepository.delete(voucher);
    }

    private String normalizeCode(String code) {
        // Code duoc upper-case de tranh duplicate do khac biet hoa/thuong.
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("Voucher code không được để trống");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private Voucher.Scope parseScope(String scope) {
        // Mac dinh STORE neu client khong truyen scope.
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
        // Voucher theo san pham bat buoc co productId hop le.
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
        // Don vi la % va gioi han toi da 100.
        if (discountPercent == null || discountPercent <= 0 || discountPercent > 100) {
            throw new RuntimeException("discountPercent phải nằm trong khoảng (0, 100]");
        }
    }

    private VoucherResponse toVoucherResponse(Voucher voucher) {
        // DTO hoa de FE nhan du lieu gon va on dinh.
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
