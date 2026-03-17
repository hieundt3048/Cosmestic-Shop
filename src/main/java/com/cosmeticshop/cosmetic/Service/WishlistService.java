package com.cosmeticshop.cosmetic.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.WishlistItemResponse;
import com.cosmeticshop.cosmetic.Entity.Product;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Entity.WishlistItem;
import com.cosmeticshop.cosmetic.Exception.ResourceNotFoundException;
import com.cosmeticshop.cosmetic.Repository.ProductRepository;
import com.cosmeticshop.cosmetic.Repository.UserRepository;
import com.cosmeticshop.cosmetic.Repository.WishlistItemRepository;

import jakarta.transaction.Transactional;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final IAuditLogService auditLogService;

    public WishlistService(
            WishlistItemRepository wishlistItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            IAuditLogService auditLogService) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    public List<WishlistItemResponse> getMyWishlist(String username) {
        User user = getCustomerByUsername(username);
        // Chỉ trả về sản phẩm còn hiển thị để tránh lộ các sản phẩm đã bị ẩn khỏi catalog.
        return wishlistItemRepository.findByUserIdOrderByAddedAtDesc(user.getId()).stream()
                .filter(item -> item.getProduct() != null && !Boolean.FALSE.equals(item.getProduct().getVisible()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WishlistItemResponse addToWishlist(String username, Long productId) {
        User user = getCustomerByUsername(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với id: " + productId));

        if (Boolean.FALSE.equals(product.getVisible())) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm với id: " + productId);
        }

        Optional<WishlistItem> existingItem = wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId);
        // Đảm bảo thao tác thêm là idempotent: đã có thì trả về luôn, không tạo bản ghi trùng.
        if (existingItem.isPresent()) {
            return toResponse(existingItem.get());
        }

        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setUser(user);
        wishlistItem.setProduct(product);

        WishlistItem savedItem = wishlistItemRepository.save(wishlistItem);
        auditLogService.logAction(
                "ADD_TO_WISHLIST",
                "user#" + user.getId(),
                "Them san pham vao wishlist: product#" + productId);

        return toResponse(savedItem);
    }

    @Transactional
    public void removeFromWishlist(String username, Long productId) {
        User user = getCustomerByUsername(username);
        Optional<WishlistItem> existingItem = wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId);

        // Đảm bảo thao tác xóa là idempotent: item không tồn tại thì bỏ qua, không phát sinh lỗi.
        if (existingItem.isEmpty()) {
            return;
        }

        wishlistItemRepository.delete(existingItem.get());
        auditLogService.logAction(
                "REMOVE_FROM_WISHLIST",
                "user#" + user.getId(),
                "Xoa san pham khoi wishlist: product#" + productId);
    }

    private User getCustomerByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));

        // Wishlist là tính năng phía khách mua hàng, chỉ cho phép tài khoản CUSTOMER sử dụng.
        if (user.getRole() != User.Role.CUSTOMER) {
            throw new RuntimeException("Chỉ tài khoản CUSTOMER mới có thể sử dụng wishlist");
        }

        return user;
    }

    private WishlistItemResponse toResponse(WishlistItem wishlistItem) {
        Product product = wishlistItem.getProduct();
        String brandName = product.getBrand() == null ? null : product.getBrand().getName();

        return new WishlistItemResponse(
                wishlistItem.getId(),
                product.getId(),
                product.getName(),
                brandName,
                product.getPrice(),
                product.getImageUrl(),
                product.getStockQuantity(),
                wishlistItem.getAddedAt());
    }
}
